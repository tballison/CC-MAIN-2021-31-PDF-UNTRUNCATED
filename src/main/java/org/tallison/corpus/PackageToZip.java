package org.tallison.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageToZip {
    static Logger LOGGER = LoggerFactory.getLogger(PackageToZip.class);


    public static void main(String[] args) throws Exception {
        File config = new File(args[0]);
        PackageConfig packageConfig = new ObjectMapper().readValue(config, PackageConfig.class);
        ArrayBlockingQueue<Integer> zips =
                new ArrayBlockingQueue<>(packageConfig.getToZip() - packageConfig.getFromZip() + 2);
        for (int i = packageConfig.getFromZip(); i <= packageConfig.getToZip(); i++) {
            zips.add(i);
        }
        zips.add(-1);
        ExecutorService ex = Executors.newFixedThreadPool(packageConfig.getNumThreads());
        ExecutorCompletionService<Integer> executorCompletionService =
                new ExecutorCompletionService<>(ex);
        for (int i = 0; i < packageConfig.getNumThreads(); i++) {
            executorCompletionService.submit(new ZipWorker(packageConfig, zips));
        }

        int finished = 0;
        while (finished < packageConfig.getNumThreads()) {
            //blocking
            Future<Integer> future = executorCompletionService.take();
            try {
                future.get();
                finished++;
            } catch (Exception e) {
                LOGGER.error("fatal error", e);
                ex.shutdownNow();
                System.exit(1);
            }
        }

        ex.shutdownNow();
    }

    private static class ZipWorker implements Callable<Integer> {
        private static AtomicInteger COUNTER = new AtomicInteger(0);

        private final int id = COUNTER.getAndIncrement();
        private final PackageConfig config;
        private final AmazonS3 fetchClient;
        private final AmazonS3 writeClient;
        private final ArrayBlockingQueue<Integer> zips;

        public ZipWorker(PackageConfig config, ArrayBlockingQueue<Integer> zips) {
            this.config = config;
            this.zips = zips;
            fetchClient = loadClient(config.getSrcRegion(), config.getSrcProfile());
            writeClient = loadClient(config.getTargRegion(), config.getTargProfile());
        }

        @Override
        public Integer call() throws Exception {
            while (true) {
                //blocking
                Integer zip = zips.take();
                if (zip < 0) {
                    //blocking
                    zips.put(zip);
                    LOGGER.info("thread {} completed", id);
                    return 1;
                }
                LOGGER.info("{} thread starting to process zip: {}", id, zip);
                processSql(config, zip);
                LOGGER.info("{} thread finished processing zip: {}", id, zip);
            }
        }


        private void processFile(PackageConfig packageConfig, int id, String digest,
                                 ArchiveOutputStream aos) {
            String fetchKey = packageConfig.getSrcPrefix();
            if (fetchKey.length() > 0 && !fetchKey.endsWith("/")) {
                fetchKey += "/";
            }
            fetchKey += digest.substring(0, 2) + "/" + digest.substring(2, 4) + "/" + digest;
            Path tmp = null;
            try {
                GetObjectRequest objectRequest =
                        new GetObjectRequest(packageConfig.getSrcBucket(), fetchKey);
                tmp = Files.createTempFile("s3-tmp", "");
                ObjectMetadata objectMetadata = fetchClient.getObject(objectRequest, tmp.toFile());

                try (InputStream is = Files.newInputStream(tmp)) {
                    String cpDigest = DigestUtils.sha256Hex(is);
                    if (!cpDigest.equals(digest)) {
                        LOGGER.warn("bad digest?! {} {}", digest, cpDigest);
                    }
                }
                String entryName = StringUtils.leftPad(Integer.toString(id), 7, '0') + ".pdf";
                ArchiveEntry ae = aos.createArchiveEntry(tmp, entryName);
                ((ZipArchiveEntry) ae).setComment(digest);
                ((ZipArchiveEntry) ae).setTime(
                        FileTime.fromMillis(objectMetadata.getLastModified().getTime()));
                aos.putArchiveEntry(ae);
                Files.copy(tmp, aos);
                aos.closeArchiveEntry();
            } catch (Exception e) {
                LOGGER.error("couldn't get/copy/write " + fetchKey, e);
            } finally {
                if (tmp != null) {
                    try {
                        Files.delete(tmp);
                    } catch (IOException e) {
                        LOGGER.warn("problem deleting tmp: " + tmp, e);
                    }
                }
            }
        }


        private void processSql(PackageConfig packageConfig, int zip)
                throws Exception {
            int tries = 0;
            Exception ex = null;
            while (tries++ < 3) {
                try {
                    tryToCreateZip(packageConfig, zip);
                    return;
                } catch (Exception e) {
                    //if there was a db connectivity issue or something else
                    //wait a minute and hope for the best.
                    LOGGER.warn("Problem processing " + zip +
                        " on try=" + tries, e);
                    Thread.sleep(60000);
                    ex = e;
                }
            }
            throw ex;
        }

        private void tryToCreateZip(PackageConfig packageConfig, int zip)
            throws Exception {
            String sql = packageConfig.getSelectString();
            int pdfIdFrom = zip * 1000;
            int pdfIdTo = zip * 1000 + 1000;
            sql += " where id >= " + pdfIdFrom + " and id < " + pdfIdTo + " order by id asc";
            Path zipDir = Paths.get(packageConfig.getZipDir());

            try (Connection connection =
                DriverManager.getConnection(
                    packageConfig.getDbConnectionString())) {
                try (Statement st = connection.createStatement()) {
                    String zipName = null;
                    ArchiveOutputStream aos = null;
                    try (ResultSet rs = st.executeQuery(sql)) {
                        while (rs.next()) {
                            int id = rs.getInt(1);
                            String digest = rs.getString(2);
                            String nextZipName = getZipName(id);
                            LOGGER.info("{} {} {} {}", id, digest, zipName, nextZipName);
                            if (!nextZipName.equals(zipName)) {
                                finishZip(packageConfig, aos, zipDir, zipName);
                                aos = getArchiveOutputStream(zipDir, nextZipName);
                            }
                            processFile(packageConfig, id, digest, aos);
                            zipName = nextZipName;
                        }
                    }
                    finishZip(packageConfig, aos, zipDir, zipName);
                }
            }
        }


        private void finishZip(PackageConfig packageConfig, ArchiveOutputStream aos, Path zipDir,
                               String zipName) {
            try {
                _finishZip(packageConfig, aos, zipName);
            } catch (IOException e) {
                LOGGER.error("couldn't finish zip " + zipName, e);
            }
        }

        private void _finishZip(PackageConfig packageConfig, ArchiveOutputStream aos,
                                String zipName) throws IOException {
            if (aos == null) {
                return;
            }
            Path zipDir = Paths.get(packageConfig.getZipDir());
            LOGGER.info("about to finish zip {}", zipName);
            aos.finish();
            aos.close();

            Path zip = zipDir.resolve(zipName);
            long zipSize = Files.size(zip);
            LOGGER.info("about to copy " + zip);
            String zipSha = "";
            String zipMd5 = "";
            try (InputStream is = Files.newInputStream(zip)) {
                zipSha = DigestUtils.sha256Hex(is);
            }
            try (InputStream is = Files.newInputStream(zip)) {
                zipMd5 = DigestUtils.md5Hex(is);
            }
            LOGGER.info("zip={} length={} sha256={} md5={}", zipName, zipSize, zipSha, zipMd5);
            if (packageConfig.isDryRun()) {
                return;
            }
            String targetPath = packageConfig.getTargPrefix();
            if (targetPath.length() > 0 && !targetPath.endsWith("/")) {
                targetPath += "/";
            }
            targetPath += "zipfiles/" + zipName.charAt(0) + "/" + zipName;
            LOGGER.info("writing {}", targetPath);
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(packageConfig.getTargBucket(), targetPath, zip.toFile());

            PutObjectResult result = writeClient.putObject(putObjectRequest);

            LOGGER.info("successfully wrote: {} local_md5={} aws_md5={}", targetPath,
                zipMd5, result.getContentMd5());

            if (packageConfig.isDeleteLocalZips()) {
                try {
                    Files.delete(zip);
                } catch (IOException e) {
                    LOGGER.warn("couldn't delete: " + zip, e);
                }
            }
        }
    }

    private static String getZipName(int id) {
        int val = id / 1000;
        return StringUtils.leftPad(Integer.toString(val), 4, '0') + ".zip";
    }

    private static ArchiveOutputStream getArchiveOutputStream(Path zipDir, String zipName)
            throws IOException {
        Path out = zipDir.resolve(zipName);
        if (Files.isRegularFile(out)) {
            LOGGER.warn("deleting existing zip: {}", out);
            Files.delete(out);
        }
        ArchiveOutputStream aos = new ZipArchiveOutputStream(out);
        return aos;
    }

    /*
    private static void processCsv(PackageConfig packageConfig) throws IOException {
        Path zipDir = Paths.get(packageConfig.getZipDir());
        Path csvPath = Paths.get(packageConfig.getCsvPath());
        String zipName = null;
        ArchiveOutputStream aos = null;
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String line = reader.readLine();
            while (line != null) {
                String[] cols = line.split(",");
                int id = Integer.parseInt(cols[0]);
                String digest = cols[1];
                String nextZipName = getZipName(id);
                LOGGER.info("{} {} {} {}", id, digest, zipName, nextZipName);
                if (!nextZipName.equals(zipName)) {
                    finishZip(packageConfig, aos, zipDir, zipName);
                    aos = getArchiveOutputStream(zipDir, nextZipName);
                }
                processFile(packageConfig, id, digest, aos);
                zipName = nextZipName;

            }
        }
    }*/

    private static AmazonS3 loadClient(String region, String profile) {
        AWSCredentialsProvider provider;
        if (profile != null && profile.equals("instance")) {
            provider = InstanceProfileCredentialsProvider.getInstance();
        } else if (profile != null) {
            provider = new ProfileCredentialsProvider(profile);
        } else {
            throw new IllegalArgumentException("must specify profile");
        }
        ClientConfiguration clientConfiguration = new ClientConfiguration().withMaxConnections(100);
        AmazonS3ClientBuilder amazonS3ClientBuilder =
                AmazonS3ClientBuilder.standard().withClientConfiguration(clientConfiguration)
                        .withPathStyleAccessEnabled(true).withCredentials(provider);
        amazonS3ClientBuilder.setRegion(region);
        return amazonS3ClientBuilder.build();
    }
    /*private static void execute(PackageConfig packageConfig) throws Exception {

        if (StringUtils.isAllBlank(packageConfig.getCsvPath())) {
            processSql(packageConfig);
        } else {
            processCsv(packageConfig);
        }
    }*/
}
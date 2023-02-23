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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
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
    private static AmazonS3 FETCH_CLIENT;
    private static AmazonS3 WRITE_CLIENT;

    public static void main(String[] args) throws Exception {
        File config = new File(args[0]);
        PackageConfig packageConfig = new ObjectMapper().readValue(config, PackageConfig.class);

        FETCH_CLIENT = loadClient(packageConfig.getSrcRegion(), packageConfig.getSrcProfile());
        WRITE_CLIENT = loadClient(packageConfig.getTargRegion(), packageConfig.getTargProfile());
        execute(packageConfig);
    }

    private static AmazonS3 loadClient(String region, String profile) {
        AWSCredentialsProvider provider;
        if (profile != null && profile.equals("instance")) {
            provider = InstanceProfileCredentialsProvider.getInstance();
        } else if (profile != null) {
            provider = new ProfileCredentialsProvider(profile);
        } else {
            throw new IllegalArgumentException("must specify profile");
        }
        ClientConfiguration clientConfiguration = new ClientConfiguration()
                .withMaxConnections(100);
        AmazonS3ClientBuilder amazonS3ClientBuilder = AmazonS3ClientBuilder.standard()
                .withClientConfiguration(clientConfiguration)
                .withPathStyleAccessEnabled(true)
                .withCredentials(provider);
        amazonS3ClientBuilder.setRegion(region);
        return amazonS3ClientBuilder.build();
    }
    private static void execute(PackageConfig packageConfig) throws Exception {

        if (StringUtils.isAllBlank(packageConfig.getCsvPath())) {
            processSql(packageConfig);
        } else {
            processCsv(packageConfig);
        }
    }

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
                if (! nextZipName.equals(zipName)) {
                    finishZip(packageConfig, aos, zipDir, zipName);
                    aos = getArchiveOutputStream(zipDir, nextZipName);
                }
                processFile(packageConfig, id, digest, aos);
                zipName = nextZipName;

            }
        }
    }

    private static void processSql(PackageConfig packageConfig) throws SQLException, IOException {
        String sql = packageConfig.getSelectString();
        Path zipDir = Paths.get(packageConfig.getZipDir());
        try (Connection connection =
                     DriverManager.getConnection(packageConfig.getDbConnectionString())) {
            try (Statement st = connection.createStatement()) {
                String zipName = null;
                ArchiveOutputStream aos = null;
                try (ResultSet rs = st.executeQuery(sql)) {
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        String digest = rs.getString(2);
                        String nextZipName = getZipName(id);
                        LOGGER.info("{} {} {} {}", id, digest, zipName, nextZipName);
                        if (! nextZipName.equals(zipName)) {
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

    private static void processFile(PackageConfig packageConfig, int id, String digest,
                                    ArchiveOutputStream aos) {
        String fetchKey = packageConfig.getSrcPrefix();
        if (fetchKey.length() > 0 && ! fetchKey.endsWith("/")) {
            fetchKey += "/";
        }
        fetchKey += digest.substring(0,2) + "/" + digest.substring(2,4) + "/" + digest;
        Path tmp = null;
        try {
            GetObjectRequest objectRequest = new GetObjectRequest(packageConfig.getSrcBucket(), fetchKey);
            tmp = Files.createTempFile("s3-tmp", "");
            ObjectMetadata objectMetadata = FETCH_CLIENT.getObject(objectRequest, tmp.toFile());

            try (InputStream is = Files.newInputStream(tmp)) {
                String cpDigest = DigestUtils.sha256Hex(is);
                if (! cpDigest.equals(digest)) {
                    LOGGER.warn("bad digest?! {} {}", digest, cpDigest);
                }
            }
            String entryName = StringUtils.leftPad(Integer.toString(id), 7, '0') + ".pdf";
            ArchiveEntry ae = aos.createArchiveEntry(tmp, entryName);
            ((ZipArchiveEntry) ae).setComment(digest);
            ((ZipArchiveEntry) ae).setTime(FileTime.fromMillis(objectMetadata.getLastModified().getTime()));
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

    private static void finishZip(PackageConfig packageConfig, ArchiveOutputStream aos, Path zipDir,
                                  String zipName) {
        try {
            _finishZip(packageConfig, aos, zipName);
        } catch (IOException e) {
            LOGGER.error("couldn't finish zip " + zipName, e);
        }
    }

    private static void _finishZip(PackageConfig packageConfig, ArchiveOutputStream aos,
                                  String zipName) throws IOException {
        if (aos == null) {
            return;
        }
        Path zipDir = Paths.get(packageConfig.getZipDir());
        LOGGER.info("about to finish zip {} {} {}", zipName);
        aos.finish();
        aos.close();
        //TODO cp zip and then delete
        Path zip = zipDir.resolve(zipName);
        LOGGER.info("about to copy " + zip);
        String targetPath = packageConfig.getTargPrefix();
        if (targetPath.length() > 0 && ! targetPath.endsWith("/")) {
            targetPath += "/";
        }
        targetPath += zipName;
        LOGGER.info("writing {}", targetPath);
        PutObjectRequest putObjectRequest =
                new PutObjectRequest(packageConfig.getTargBucket(),
                        targetPath, zip.toFile());

        WRITE_CLIENT.putObject(putObjectRequest);
        LOGGER.info("successfully wrote: {}", targetPath);

        if (packageConfig.isDeleteLocalZips()) {
            Files.delete(zip);
        }
    }

    private static String getZipName(int id) {
        int val = id/1000;
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
}
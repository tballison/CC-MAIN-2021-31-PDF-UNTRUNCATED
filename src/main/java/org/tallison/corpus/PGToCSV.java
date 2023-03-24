package org.tallison.corpus;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.ByteOrderMark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PGToCSV {

    static Logger LOGGER = LoggerFactory.getLogger(PGToCSV.class);
    private static final int MAX_CELL_LENGTH = 32000;
    public static void main(String[] args) throws Exception {
        String connectionString = args[0];
        String sql =  "select\n" + "u.id as url_id,\n" +
                "lpad(cpr.id::varchar(12), 7, '0')||'.pdf' as file_name,\n" +
                "h.host, tld, ip_address, country, latitude, longitude\n" + "from cc_urls u\n" +
                "left join cc_fetch f on u.id=f.id\n" +
                "left join cc_corpus_ids cpr on cpr.digest=f.fetched_digest\n" +
                "left join cc_hosts h on h.id=u.host\n" + "--optional 1k file limit\n" +
                "where cpr.id < 1000\n" + "order by cpr.id, u.id";
        Path csvRoot = Paths.get("/Users/allison/data/cc/csv_tables/metadata");
        Files.createDirectories(csvRoot);
        Path csv = csvRoot.resolve("cc-hosts-20230324-1k.csv");
        int rows = 0;

        try (Connection connection = DriverManager.getConnection(connectionString)) {
            try (Statement st = connection.createStatement()) {
                try (ResultSet rs = st.executeQuery(sql)) {
                    LOGGER.info("executed query");
                    try (BufferedWriter writer = getWriter(csv)) {
                        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL)) {
                            ResultSetMetaData m = rs.getMetaData();
                            writeHeader(rs.getMetaData(), printer);
                            int cols = m.getColumnCount();
                            List<String> data = new ArrayList<>();
                            while (rs.next()) {
                                fill(rs, cols, data);
                                printer.printRecord(data);
                                data.clear();
                                if (++rows % 1000 == 0) {
                                    LOGGER.info("written {} rows", rows);
                                }
                            }
                        }
                    }
                }
            }
        }
        LOGGER.info("finished with {} ", rows);
    }

    private static BufferedWriter getWriter(Path csv) throws IOException {

        OutputStream os = Files.newOutputStream(csv);
        if (csv.getFileName().toString().endsWith(".gz")) {
            os = new GzipCompressorOutputStream(os);
        } else {
            //add boms only to the 1k files
            os.write(ByteOrderMark.UTF_8.getBytes());
        }
        return new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
    }

    private static void fill(ResultSet rs, int cols, List<String> data) throws SQLException {
        String id = "";
        for (int i = 1; i <= cols; i++) {
            String s = rs.getString(i);
            if (rs.wasNull()) {
                s = "";
            }
            if (i == 1) {
                id = s;
            }
            if (s.length() > MAX_CELL_LENGTH) {
                String snippet = s.substring(0, 30);
                int ln = s.length();
                LOGGER.warn("truncating id={} length={} first30={}", id, ln, snippet);
                s = s.substring(0, MAX_CELL_LENGTH);
            }
            data.add(s);
        }
    }

    private static void writeHeader(ResultSetMetaData metaData, CSVPrinter printer) throws
            IOException, SQLException {
        List<String> data = new ArrayList<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            data.add(metaData.getColumnLabel(i));
        }
        printer.printRecord(data);
    }
}

package com.example.loanstats.ingestion

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.io.ResourceLoader
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.math.BigDecimal
import java.time.LocalDate
import java.util.zip.GZIPInputStream

private const val BATCH_SIZE = 5000

private val CSV_FORMAT: CSVFormat = CSVFormat.DEFAULT.builder()
    .setHeader()
    .setSkipHeaderRecord(true)
    .get()

private data class LoanRow(
    val loanAmnt: BigDecimal?,
    val intRate: BigDecimal?,
    val grade: String,
    val issueDate: LocalDate,
    val purpose: String?,
    val state: String?,
    val dti: BigDecimal?,
    val ficoScore: Int?,
    val ficoBand: String?,
)

@Component
class LoanLoader(
    private val jdbc: NamedParameterJdbcTemplate,
    private val resourceLoader: ResourceLoader,
    @Value("\${loans.csv.path}") private val csvPath: String,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(LoanLoader::class.java)

    override fun run(args: ApplicationArguments) {
        val resource = resourceLoader.getResource(csvPath)
        if (!resource.exists() || !resource.isReadable) {
            throw IllegalStateException("Cannot read loans file at $csvPath. Set loans.csv.path to a valid file.")
        }

        val (read, loaded) = resource.inputStream.use { load(it) }

        if (loaded == 0) {
            throw IllegalStateException("No loans were loaded from $csvPath. Check the file format.")
        }
        log.info("Loaded {} of {} rows from {} ({} dropped)", loaded, read, csvPath, read - loaded)
    }

    private fun load(stream: InputStream): Pair<Int, Int> {
        val input = if (csvPath.endsWith(".gz", ignoreCase = true)) GZIPInputStream(stream) else stream
        val reader = BufferedReader(InputStreamReader(input, Charsets.UTF_8))
        reader.readLine() // line 1 is a note

        return CSV_FORMAT.parse(reader).use { parser ->
            var read = 0
            var loaded = 0
            val batch = ArrayList<LoanRow>(BATCH_SIZE)
            for (record in parser) {
                read++
                val row = toRow(record) ?: continue
                batch.add(row)
                if (batch.size == BATCH_SIZE) {
                    insert(batch)
                    loaded += batch.size
                    batch.clear()
                }
            }
            if (batch.isNotEmpty()) {
                insert(batch)
                loaded += batch.size
            }
            read to loaded
        }
    }

    private fun insert(rows: List<LoanRow>) {
        val sql = """
            INSERT INTO loans (loan_amnt, int_rate, grade, issue_d, purpose, addr_state, dti, fico_range_low, fico_band)
            VALUES (:loanAmnt, :intRate, :grade, :issueDate, :purpose, :state, :dti, :ficoScore, :ficoBand)
        """

        jdbc.batchUpdate(sql, SqlParameterSourceUtils.createBatch(rows))
    }

    private fun toRow(record: CSVRecord): LoanRow? {
        if (!record.isConsistent) return null

        val grade = cleanText(record.get("grade"))
        val issueDate = parseMonth(record.get("issue_d"))
        if (grade == null || issueDate == null) return null

        val score = parseInt(record.get("fico_range_low"))
        return LoanRow(
            loanAmnt = parseDecimal(record.get("loan_amnt")),
            intRate = parsePercent(record.get("int_rate")),
            grade = grade,
            issueDate = issueDate,
            purpose = cleanText(record.get("purpose")),
            state = cleanState(record.get("addr_state")),
            dti = cleanDti(record.get("dti")),
            ficoScore = score,
            ficoBand = ficoBand(score),
        )
    }
}

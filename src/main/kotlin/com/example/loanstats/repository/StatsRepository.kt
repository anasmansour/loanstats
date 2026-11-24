package com.example.loanstats.repository

import com.example.loanstats.model.GroupBy
import com.example.loanstats.model.StatsFilter
import com.example.loanstats.model.StatsRow
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

private val STATS_ROW_MAPPER = RowMapper { rs, _ ->
    StatsRow(
        group = rs.getString("group_key"),
        count = rs.getLong("count"),
        totalAmount = rs.getBigDecimal("total_amount"),
        avgAmount = rs.getBigDecimal("avg_amount"),
        avgRate = rs.getBigDecimal("avg_rate"),
        medianRate = rs.getBigDecimal("median_rate"),
        avgDti = rs.getBigDecimal("avg_dti"),
    )
}

@Repository
class StatsRepository(private val jdbc: NamedParameterJdbcTemplate) {

    fun stats(filter: StatsFilter, groupBy: GroupBy? = null): List<StatsRow> {
        val conditions = mutableListOf<String>()
        val params = MapSqlParameterSource()

        filter.start?.let { conditions += "issue_d >= :start"; params.addValue("start", it) }
        filter.end?.let { conditions += "issue_d <= :end"; params.addValue("end", it) }
        filter.state?.let { conditions += "addr_state = :state"; params.addValue("state", it) }
        filter.grade?.let { conditions += "grade = :grade"; params.addValue("grade", it) }
        filter.ficoBand?.let { conditions += "fico_band = :ficoBand"; params.addValue("ficoBand", it) }

        val where = if (conditions.isEmpty()) "" else "WHERE " + conditions.joinToString(" AND ")

        val groupKey = groupBy?.sql ?: "NULL"
        val groupClause = groupBy?.let { "GROUP BY ${it.sql} ORDER BY ${it.orderBy}" } ?: ""

        val sql = """
            SELECT $groupKey AS group_key,
                   COUNT(*)                      AS count,
                   SUM(loan_amnt)                AS total_amount,
                   ROUND(AVG(loan_amnt), 2)      AS avg_amount,
                   ROUND(AVG(int_rate), 2)       AS avg_rate,
                   ROUND(MEDIAN(int_rate), 2)    AS median_rate,
                   ROUND(AVG(dti), 2)            AS avg_dti
            FROM loans
            $where
            $groupClause
        """

        return jdbc.query(sql, params, STATS_ROW_MAPPER)
    }
}

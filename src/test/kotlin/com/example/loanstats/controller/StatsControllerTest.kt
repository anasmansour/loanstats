package com.example.loanstats.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
class StatsControllerTest(@Autowired context: WebApplicationContext) {

    private val mockMvc: MockMvc = MockMvcBuilders.webAppContextSetup(context).build()

    @Test
    fun `no parameters returns one overall row with the response fields`() {
        mockMvc.perform(get("/loans/stats"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].group").doesNotExist())
            .andExpect(jsonPath("$[0].count").value(13))
            .andExpect(jsonPath("$[0].totalAmount").exists())
            .andExpect(jsonPath("$[0].avgAmount").exists())
            .andExpect(jsonPath("$[0].avgRate").exists())
            .andExpect(jsonPath("$[0].medianRate").exists())
            .andExpect(jsonPath("$[0].avgDti").exists())
    }

    @Test
    fun `groupBy returns one row per group in ascending order`() {
        mockMvc.perform(get("/loans/stats").param("groupBy", "grade"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(5))
            .andExpect(jsonPath("$[0].group").value("A"))
            .andExpect(jsonPath("$[0].count").value(3))
            .andExpect(jsonPath("$[4].group").value("E"))
    }
}

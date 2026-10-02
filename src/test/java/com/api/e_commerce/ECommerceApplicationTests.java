package com.api.e_commerce;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class ECommerceApplicationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
		Integer appliedMigrations = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where success = true and version is not null",
				Integer.class);
		Integer identityTable = jdbcTemplate.queryForObject(
				"select count(*) from information_schema.tables "
						+ "where table_schema = current_schema() and table_name = 'user_identities'",
				Integer.class);

		assertEquals(13, appliedMigrations);
		assertEquals(1, identityTable);
	}

}

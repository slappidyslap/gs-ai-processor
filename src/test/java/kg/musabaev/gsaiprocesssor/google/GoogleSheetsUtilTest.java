package kg.musabaev.seogooglesheetshelper.google;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GoogleSheetsUtilTest {

	@Test
	void getRowCount() {
		assertEquals(11, GoogleSheetsUtil.getRowCount("A5:A15"));
		assertEquals(10, GoogleSheetsUtil.getRowCount("A1:A10"));
	}
}
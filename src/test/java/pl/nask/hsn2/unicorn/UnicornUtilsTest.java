package pl.nask.hsn2.unicorn;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pl.nask.hsn2.unicorn.connector.UnicornUtils;

public class UnicornUtilsTest {

		private static final long ZERO_DATE = 0;
		private static final long DATE_2001_01_02_1238_01 = 1332345423234L;
		private TimeZone defaultTimeZone;

		@Before
		public void setGmtTimeZone() {
			this.defaultTimeZone = TimeZone.getDefault();
			TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
		}
		
		@Test
		public void fotmattedDateTimeTest() {
			Assert.assertEquals("1970-01-01 00:00:00.000", UnicornUtils.formattedDateTime(ZERO_DATE));;
			Assert.assertEquals("2012-03-21 15:57:03.234", UnicornUtils.formattedDateTime(DATE_2001_01_02_1238_01));;
		}
		
		@After
		public void resetTimeZoneToDefault() {
			TimeZone.setDefault(defaultTimeZone);
		}
}

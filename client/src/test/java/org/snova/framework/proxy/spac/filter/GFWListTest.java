package org.snova.framework.proxy.spac.filter;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GFWListTest
{
	
	@Test
	public void testIsBlockedByGFW() throws IOException
	{
		InputStream fis = getClass().getResourceAsStream("/gfwlist.txt");
		BufferedReader br  = new BufferedReader(new InputStreamReader(fis));
		StringBuilder buffer = new StringBuilder();
		while(true)
		{
			String line = br.readLine();
			if(null == line)
			{
				break;
			}
			buffer.append(line);
		}
		GFWList.loadRules(buffer.toString());
		long start = System.currentTimeMillis();
		boolean ret = GFWList.getInstacne().isBlockedByGFW("http://www.sina.com");
		assertFalse(ret);
		ret = GFWList.getInstacne().isBlockedByGFW("https://www.youtube.com");
		assertTrue(ret);
		ret = GFWList.getInstacne().isBlockedByGFW("http://kmh.gov.tw");
		assertFalse(ret);
		ret = GFWList.getInstacne().isBlockedByGFW("https://www.youtube.com");
		assertTrue(ret);
		long end = System.currentTimeMillis();
		System.out.println("####Cost " + (end-start) + "ms");
	}
	
}

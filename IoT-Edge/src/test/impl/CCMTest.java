package impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CCMTest {
	
	@Test
	public void testCcmaesEncrypt() {
		boolean encrypt = true;
		String plain ="hello";
		String key="test123";
		String iv="0";
		assertEquals("���W�M�2C&�", CCMImpl.ccmaes(encrypt, plain, key, iv));
		
	}
	
	@Test
	public void testCcmaesDecrypt() {
		boolean encrypt = false;
		String plain ="���W�M�2C&�";
		String key="test123";
		String iv="0";
		assertEquals("hello", CCMImpl.ccmaes(encrypt, plain, key, iv));
	}
}

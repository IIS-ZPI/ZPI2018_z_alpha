package com.zpi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.jupiter.api.Test;

public class MainTest {

	@Test
	public void fetchDataFromAPI() {
		
		try{
            URL url = new URL("http://api.nbp.pl/api/exchangerates/tables/A/");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            String current;
            
        }catch(IOException e){
        	
        }
		
		//assertEquals(fetchData(), in.read());
	}
}

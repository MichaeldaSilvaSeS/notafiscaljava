package notafiscal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Principal {
	public static CloseableHttpClient createClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException{
		SSLContextBuilder builder = new SSLContextBuilder();
		builder.loadTrustMaterial(null, new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		});

		SSLConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(builder.build(),
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		
		BasicCookieStore cookieStore = new BasicCookieStore();
		
		return HttpClients.custom().setSSLSocketFactory(sslSF).setDefaultCookieStore(cookieStore) .build();		
	}
	
	public static Document getWebSite(HttpClient client, String url) throws ClientProtocolException, IOException{
		HttpResponse response = client.execute(new HttpGet(url));
		return Jsoup.parse(EntityUtils.toString(response.getEntity()));
	}
	
	public static Document postWebSite(HttpClient client, List <NameValuePair> pairsValues, String url) throws ClientProtocolException, IOException{
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new UrlEncodedFormEntity(pairsValues));
		httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
		httpPost.setHeader("Accept-Language", "gzip, deflate, br");
		httpPost.setHeader("Cache-Control", "max-age=0");
		httpPost.setHeader("Connection", "keep-alive");
		httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
		httpPost.setHeader("Host", "www.nfe.fazenda.gov.br");
		httpPost.setHeader("Origin", "https://www.nfe.fazenda.gov.br");
		httpPost.setHeader("Referer", "https://www.nfe.fazenda.gov.br/portal/consulta.aspx?tipoConsulta=completa&tipoConteudo=XbSeqxE8pl8%3d");
		httpPost.setHeader("Upgrade-Insecure-Requests", "1");
		//httpPost.
				
		HttpResponse response = client.execute(httpPost);
		return Jsoup.parse(EntityUtils.toString(response.getEntity()));
	}
	
	
	public static String readKeyboard(){
		Scanner scan = new Scanner(System.in);
		String line = null;
		while (line == null && scan.hasNext())
			line = scan.nextLine();
		return line;
	}
	
	public static void writeFile(byte[] data, String filename, String extension) throws IOException{
		OutputStream fileCaptcha = new FileOutputStream(
				new File(System.getProperty("user.home") + File.separator + filename+".".concat(extension)));
		fileCaptcha.write(data);
		fileCaptcha.close();		
	}
	
	public static Map<String,String> extractFormElements(Document document){
		Map<String, String> map = new HashMap<String,String>();
		String[] formElements = {
			"__EVENTTARGET","__EVENTARGUMENT","__VIEWSTATE","__VIEWSTATEGENERATOR","__EVENTVALIDATION",
			"ctl00$txtPalavraChave","ctl00$ContentPlaceHolder1$txtChaveAcessoCompleta",
			"ctl00$ContentPlaceHolder1$txtCaptcha","ctl00$ContentPlaceHolder1$btnConsultar",
			"ctl00$ContentPlaceHolder1$token","ctl00$ContentPlaceHolder1$captchaSom",
			"hiddenInputToUpdateATBuffer_CommonToolkitScripts"
		};
		//12
		String[] valueElements = {"","","","","","","35170400199517000151550010000002541000090001","","","","","1"};
		for(int index = 0; index < formElements.length; index++)
			map.put(formElements[index], valueElements[index]);
		
		for(String formElement: formElements){
			Element element = document.getElementsByAttributeValue("name", formElement).first();
			if(element != null && (!element.val().isEmpty()) )
				map.put(formElement, element.val());
		}
		return map;
	}
	
	public static String filename(){
		String filename = String.valueOf(Math.abs(Math.random())).replace(".", "").replace(",", "").replace("E", "");
		return filename;
	}
	
	public static void main(String[] args)
			throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, InterruptedException {
		
		CloseableHttpClient client = createClient();
		try{
			Map<String,String> map = Collections.emptyMap();
			{
				String url = "https://www.nfe.fazenda.gov.br/portal/consulta.aspx?tipoConsulta=completa&tipoConteudo=XbSeqxE8pl8=";
				Document document = getWebSite(client, url);
				
				String captchaSrc = document.getElementById("ctl00_ContentPlaceHolder1_imgCaptcha").attr("src");
				String captchaExtensao = captchaSrc.substring(captchaSrc.indexOf("/") + 1, captchaSrc.indexOf(";"));
				captchaSrc = captchaSrc.substring(captchaSrc.indexOf(",") + 1, captchaSrc.length());
				byte[] captchaImg = DatatypeConverter.parseBase64Binary(captchaSrc);
				
				writeFile(captchaImg, filename(), captchaExtensao);
				map = extractFormElements(document);
				
			}
			
			System.out.println("Informe o numero da nota: ");
			String nota = readKeyboard();
			System.out.println("Informe o captcha: ");
			String captcha = readKeyboard();
			
			{
				String url = "https://www.nfe.fazenda.gov.br/portal/consulta.aspx?tipoConsulta=completa&tipoConteudo=XbSeqxE8pl8=";
				List <NameValuePair> pairsValues = new ArrayList <NameValuePair>();
				for(Map.Entry<String, String> pairValue : map.entrySet()){
					BasicNameValuePair bnvp = new BasicNameValuePair(pairValue.getKey(),captcha);
					if(pairValue.getKey().equals("ctl00$ContentPlaceHolder1$txtCaptcha"))
						bnvp = new BasicNameValuePair(pairValue.getKey(),captcha);
					else if(pairValue.getKey().equals("ctl00$ContentPlaceHolder1$txtChaveAcessoCompleta"))
						bnvp = new BasicNameValuePair(pairValue.getKey(),nota);
					else
						bnvp = new BasicNameValuePair(pairValue.getKey(),pairValue.getValue());
					pairsValues.add(bnvp);
				}
				Document document = postWebSite(client, pairsValues, url);		
			}
			{
				String url = "https://www.nfe.fazenda.gov.br/portal/consultaCompleta.aspx?tipoConteudo=XbSeqxE8pl8=";
				Document document = getWebSite(client, url);
				Elements es = document.select("#NFe fieldset:nth-of-type(2) table tbody tr td:nth-of-type(2) span");
				System.out.println("Nome / Razão Social: "+es.first().html());			
			}
		}catch(Exception e){
			System.out.println("Ocorreu um erro");
		}finally{
			client.close();
		}
	}

}

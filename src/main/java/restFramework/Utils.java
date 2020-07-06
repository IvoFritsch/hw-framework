/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author ivoaf
 */
public class Utils {
    
    /**
     * Retorna a diferença entre duas datas
     *  O valor é positivo se date2 for posterior à date1
     * 
     * @param date1 Data 1
     * @param date2 Data 2
     * @param timeUnit Unidade de tempo para retornar a resposta
     * @return Diferença entre as datas
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }
    
    /**
     * Gera um hash MD5 a partir da string de origem, fazendo a quantidade de passagens especificada.
     * 
     * @param original String original da qual o Hash deve ser gerado
     * @param passes Quantidade de passagens a fazer, quanto maior esse valor, mais forte é a encriptação, mas mais tempo leva para ser gerada
     * @return A string hash gerada
     */
    public static String generateMD5Hash(String original, int passes){
        MessageDigest encrypter;
        try {
            encrypter = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            return null;
        }
        byte[] digerido = encrypter.digest(original.getBytes());
        passes--;
        for (; passes > 0; passes--) {
            digerido = encrypter.digest(digerido);
        }
        
        // Convert byte array into signum representation 
        BigInteger no = new BigInteger(1, digerido); 

        // Convert message digest into hex value 
        String hashtext = no.toString(16); 
        while (hashtext.length() < 32) { 
            hashtext = "0" + hashtext; 
        } 
        return hashtext;
    }
    
    /**
     * Faz um request GET na URL e retorna o conteúdo
     * 
     * @param urlToFetch URL para fazer o request
     * @return conteúdo retornado pela URL
     */
    public static String fetch(String urlToFetch){
    StringBuilder result = new StringBuilder();
    URL url;
    try {
        url = new URL(urlToFetch);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
        String line;
        while ((line = rd.readLine()) != null) {
         result.append(line);
        }
        rd.close();

    } catch (Exception ex) {
        ex.printStackTrace();
        return null;
    }
    return result.toString();
    }
    
    public static String encryptAES(String text, String key) throws Exception {
        // create key and cipher
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        // encrypt the text
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(text.getBytes());

        return bytesToHex(encrypted);
    }
    
    
    public static String decryptAES(String hex, String key) throws Exception {
        // create key and cipher
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        // transform hex to bytes
        byte[] decHex = hexToBytes(hex);
        // decrypt the text
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        return new String(cipher.doFinal(decHex));
    }
    
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    private static byte[] hexToBytes(String hex) {
        byte[] val = new byte[hex.length() / 2];
        for (int i = 0; i < val.length; i++) {
           int index = i * 2;
           int j = Integer.parseInt(hex.substring(index, index + 2), 16);
           val[i] = (byte) j;
        }
        
        return val;
    }
    
    public static String cropString(String s, int size){
        return cropString(s, size, false);
    }
    
    public static String cropString(String s, int size, boolean ellipsis){
        boolean putEllipsis = false;
        if(ellipsis && s.length() > size){
            size -= 3;
            putEllipsis = true;
        }
        int maxLength = (s.length() < size)? s.length(): size;
        s = s.substring(0, maxLength);
        if(putEllipsis) s += "...";
        return s;
    }
    
    public static String removeAccents(String s){
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                        .replaceAll("[^\\p{ASCII}]", "");
    }
    
    public static String applyMask(String str, String mask) {
        try{
            javax.swing.text.MaskFormatter mf = new javax.swing.text.MaskFormatter(mask);
            mf.setValueContainsLiteralCharacters(false);
            return mf.valueToString(str);
        } catch (Exception e){
            return str;
        }
   }
    
    public static String geraChave6Digitos(){
        String AB = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder( 6 );
        for( int i = 0; i < 6; i++ ) 
           sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
        
    }
    
    public static String geraChaveNDigitos(int qtdDigitos){
        String AB = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder( 6 );
        for( int i = 0; i < qtdDigitos; i++ ) 
           sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
        
    }
    
    public static String geraChave6Numeros(){
        String AB = "123456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder( 6 );
        for( int i = 0; i < 6; i++ ) 
           sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
        
    }
    
    /**
     * Retorna o inicio da data de Hoje
     * 
     * @return
     */
    @SuppressWarnings( "deprecation" )
    public static Date hoje(){
        Date hoje = new Date();
        hoje.setHours(0);
        hoje.setMinutes(0);
        hoje.setSeconds(0);
        return hoje;
    }
    
    /**
     * Retorna se duas data estão no mesmo dia
     * 
     * @param date1
     * @param date2
     * @return
     */
    public static boolean sameDay(Date date1, Date date2){
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                          cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }
    
    
    public static String getFullStackTraceHtml(Exception e){
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        String stringErros = errors.toString();
        stringErros = stringErros.substring(stringErros.indexOf("Caused by: ")+"Caused by: ".length());
        return stringErros.replace("\n", "<br/>").replace("\t", "&emsp;&emsp;");
    }
    
    public static String findHsqlmanDbLocation(String dbName){
        if(RestApi.HW_PRODUCTION) return "/var/apps/"+RestApi.nomeProjeto+"/db/";
        if(dbName == null) return null;
        return getHsqlmanDbProperty("location",dbName);
    }
    
    private static String getHsqlmanDbProperty(String property, String dbName){
        if(!hsqlmanAvailabilityCheck()){
            return null;
        }
        String url = "http://" + (RestApi.HW_PRODUCTION ? "banco-"+RestApi.nomeProjeto : "localhost") + ":"+1111+"/";
        URL obj;
        HttpURLConnection con;
        try {
            obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();
        } catch (Exception ex) {
            return null;
        }
        try {
            // optional default is GET
            con.setRequestMethod("POST");
        } catch (ProtocolException ex) {
        }
        con.setDoOutput(true);
        try {
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes("{command:\"query_"+property+"\", name:\""+(RestApi.HW_PRODUCTION ? "db" : dbName)+"\"}");
            wr.flush();
            wr.close();
            con.getResponseCode();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
                response.append("\n");
            }
            in.close();
            if(response.toString().trim().equals("none"))
                return null;
            return response.toString().trim();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    private static boolean hsqlmanAvailabilityCheck() { 
        if(RestApi.HW_PRODUCTION) return true;
        try (Socket s = new Socket((RestApi.HW_PRODUCTION ? "banco-"+RestApi.nomeProjeto : "localhost"), 1111)) {
            return true;
        } catch (IOException ex) {
            /* ignore */
        }
        return false;
    }
    
    
    public static class Day {

        private final ZoneId TZ = ZoneId.of("America/Sao_Paulo");

        private ZonedDateTime zdt;
        private Instant instant;

        public Day() {
            this(System.currentTimeMillis());
        }
        
        public Day(long millis) {
            instant = Instant.ofEpochMilli(millis);
            zdt = ZonedDateTime.ofInstant( instant , TZ );
        }

        public Date getStart() {
            return Date.from(zdt.with(LocalTime.MIN).toInstant());
        }

        public Date getEnd() {
            return Date.from(zdt.with(LocalTime.MAX).toInstant());
        }
        
        public Day previous(){
            return new Day(instant.minusMillis(TimeConstants.DAY).toEpochMilli());
        }
        
        public Day next(){
            return new Day(instant.plusMillis(TimeConstants.DAY).toEpochMilli());
        }

        public int getDayOfMonth(){
            return zdt.getDayOfMonth();
        }
        
        @Override
        public String toString() {
            return String.format("Day (%s) -> (%s) -> "+getDayOfMonth(),
                                this.getStart(),
                                this.getEnd());
        }
    }
    
    public static class Week {

        private final ZoneId TZ = ZoneId.of("America/Sao_Paulo");

        private final Locale locale;
        private final DayOfWeek firstDayOfWeek;
        private final DayOfWeek lastDayOfWeek;
        private Instant instant;
        private Date start;
        private Date end;

        public Week() {
            this(System.currentTimeMillis());
        }
        
        public Week(long millis) {
            this.instant = Instant.ofEpochMilli(millis);
            this.locale = Locale.US;
            this.firstDayOfWeek = WeekFields.of(locale).getFirstDayOfWeek();
            this.lastDayOfWeek = DayOfWeek.of(((this.firstDayOfWeek.getValue() + 5) % DayOfWeek.values().length) + 1);
        }

        public Date getStart() {
            if(start != null) return start;
            ZoneOffset zoneOffset = TZ.getRules().getOffset(instant);
            return Date.from(LocalDateTime.ofInstant(instant, zoneOffset).toLocalDate().with(TemporalAdjusters.previousOrSame(this.firstDayOfWeek)).atStartOfDay().toInstant(zoneOffset));
        }

        public Date getEnd() {
            if(end != null) return end;
            ZoneOffset zoneOffset = TZ.getRules().getOffset(instant);
            return Date.from(LocalDateTime.ofInstant(instant, zoneOffset).toLocalDate().with(TemporalAdjusters.nextOrSame(this.lastDayOfWeek)).atTime(LocalTime.MAX).toInstant(zoneOffset));
        }
        
        public Week previous(){
            return new Week(instant.minusMillis(TimeConstants.WEEK).toEpochMilli());
        }
        
        public Week next(){
            return new Week(instant.plusMillis(TimeConstants.WEEK).toEpochMilli());
        }

        @Override
        public String toString() {
            return String.format("Week (%s) -> (%s)",
                                this.getStart(),
                                this.getEnd());
        }
    }
}

package pri.wx;

import pri.wx.jwcrawler.method.JwMain;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        String result = JwMain.apiClasses("swe15033-neyywrru", "20161").toString();
        System.out.println(result);
    }
}

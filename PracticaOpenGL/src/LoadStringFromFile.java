import java.io.BufferedReader;
import java.io.FileReader;

public class LoadStringFromFile {

	public static String loadStringFromFile( String path )
	{
		try {
			BufferedReader reader = new BufferedReader( new FileReader( path ));
			StringBuilder stringBuilder = new StringBuilder( );
			String line = null;
			String ls = System.getProperty("line.separator");
			while(( line = reader.readLine( )) != null )
			{
				stringBuilder.append( line );
				stringBuilder.append(ls);
			}
			stringBuilder.deleteCharAt( stringBuilder.length() - 1 );
			reader.close( );
			return stringBuilder.toString( );
		}
		catch( Exception ex )
		{
			return new String( "" );
		}
	}
}


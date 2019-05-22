import java.util.Arrays;

public class MatrixMaths {

	private static float _identity[] = {
			1.0f,	0.0f,	0.0f,	0.0f,
			0.0f,	1.0f,	0.0f,	0.0f,
			0.0f,	0.0f,	1.0f,	0.0f,
			0.0f,	0.0f,	0.0f,	1.0f 
	};
	

	public static void loadIdentity ( float[] matrix )
	{
		for ( int i = 0; i < 16; i++ )
			matrix[i] = _identity[i];
	}
	
	public static void buildProjectionMatrix( 
			float[] matrix, float fov, float ratio, 
			float nearPlane, float farPlane )
	{
		loadIdentity( matrix );
		float f = 1.0f / (float)(Math.tan( fov*(3.141599 / 360.0)));
		
		matrix[0] = f / ratio;	
		matrix[1 * 4 + 1] = f;
		matrix[2 * 4 + 2] = (farPlane + nearPlane) / (nearPlane - farPlane);
		matrix[3 * 4 + 2] = (2.0f * farPlane * nearPlane) / (nearPlane - farPlane);
		matrix[2 * 4 + 3] = -1.0f;
		matrix[3 * 4 + 3] = 0.0f;
	}


	static void translate(float[] matrix, float x, float y, float z)
	{	
		matrix[12] = matrix[0] * x +  matrix[4] * y + 
				matrix[8] * z + matrix[12];
		matrix[13] = matrix[1] * x +  matrix[5] * y + 
				matrix[9] * z + matrix[13];
		matrix[14] = matrix[2] * x +  matrix[6] * y + 
				matrix[10] * z + matrix[14];
	}	

	static void rotateX(float[] matrix, double angle)
	{
		float c = (float)Math.cos(angle);
		float s = (float)Math.sin(angle);

		float t4 = matrix[4];
		float t5 = matrix[5];
		float t6 = matrix[6];

		matrix[4] = t4 * c + matrix[8] * s;
		matrix[5] = t5 * c + matrix[9] * s;
		matrix[6] = t6 * c + matrix[10]* s;

		matrix[8] = -t4* s + matrix[8] * c;
		matrix[9] = -t5* s + matrix[9] * c;
		matrix[10]= -t6* s + matrix[10]* c;
	}	
	
	static void rotateY(float[] matrix, double angle)
	{
		float c = (float)Math.cos(angle);
		float s = (float)Math.sin(angle);

		float t0 = matrix[0];
		float t1 = matrix[1];
		float t2 = matrix[2];

		matrix[0] = t0 * c - matrix[8] * s;
		matrix[1] = t1 * c - matrix[9] * s;
		matrix[2] = t2 * c - matrix[10]* s;

		matrix[8] = t0 * s + matrix[8] * c;
		matrix[9] = t1 * s + matrix[9] * c;
		matrix[10]= t2 * s + matrix[10]* c;
	}

	static void rotateZ(float[] matrix, double angle)
	{
		float c = (float)Math.cos(angle);
		float s = (float)Math.sin(angle);

		float t0 = matrix[0];
		float t1 = matrix[1];
		float t2 = matrix[2];
		
		matrix[0] = t0 * c + matrix[4] * s;
		matrix[1] = t1 * c + matrix[5] * s;
		matrix[2] = t2 * c + matrix[6]* s;
		
		matrix[4] = -t0 * s + matrix[4] * c;
		matrix[5] = -t1 * s + matrix[5] * c;
		matrix[6] = -t2 * s + matrix[6] * c;
	}
	
}

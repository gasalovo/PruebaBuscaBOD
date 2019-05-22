import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.*;

import java.nio.*;

import javax.security.auth.Destroyable;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.opengl.GL30.*;



public class Main {
	// The window handle
	private long window;
	
	private int program;
	private int vshader;
	private int fshader;
	
    private int uProy;
    private int uModelView; 
    private int uModel; 
    private int uView; 
    private int uColorTex;
    private int uEmiTex;
    
    private int uBrillo;
    private float brillo[] = {10.0f};
    
    //Atributos
    private int inPos;
    private int inColor;
    private int inNormal;
    private int inTexCoord;
    
    private int colorID; //indice a la textura en grafica
    
    //Arrays
	int vao;
	int posVBO;
	int colorVBO;
	int normalVBO;
	int texCoordVBO;
	int triangleIndexVBO;

    private float proy[] = {
    		1.0f,	0.0f,	0.0f,	0.0f,
    		0.0f,	1.0f,	0.0f,	0.0f,
    		0.0f,	0.0f,	1.0f,	0.0f,
    		0.0f,	0.0f,	0.0f,	1.0f};

    //private float modelView[] =	{
    private float view[] =	{
    		1.0f,	0.0f,	0.0f,	0.0f,
    		0.0f,	1.0f,	0.0f,	0.0f,
    		0.0f,	0.0f,	1.0f,	0.0f,
    		0.0f,	0.0f,	0.0f,	1.0f};
    
    
    
    
    private float angle = 0.0f;
    
	int loadShader( String fileName, int type )
	{
		String source = LoadStringFromFile.loadStringFromFile( fileName );
	    //////////////////////////////////////////////
		//Creaciï¿½n y compilaciï¿½n del Shader
		int shader = glCreateShader(type);
		glShaderSource(shader, source);
		glCompileShader(shader);
	    
	     //Comprobamos que se compilo bien
	     glGetShaderInfoLog(shader);
	     int[] compiled = {0};
	     glGetShaderiv(shader, GL_COMPILE_STATUS, compiled );
	     
	     if( compiled[0] == 0 )
	     {
	    	 System.out.println( glGetShaderInfoLog( shader ));
	    	 return 0;
	     }
	        
	     System.out.println( "shader compiled");
	     return shader;
	}

	void shaderInit()
	{
	    //Creamos los shaders
		String vname = "src/vshader.glsl";
	    String fname = "src/fshader.glsl";
	      
	    //Compilacion
	    vshader = loadShader(vname,GL_VERTEX_SHADER);
	    fshader = loadShader(fname,GL_FRAGMENT_SHADER);
	       
	      //Link
	    program = glCreateProgram();
	    glAttachShader(program, vshader);
	    glAttachShader(program, fshader);
	    
	    //Puntos de entrada del programa
	    glBindAttribLocation(program, 0, "inPos");
	    glBindAttribLocation(program, 1, "inColor");
	    glBindAttribLocation(program, 2, "inNormal");
	    glBindAttribLocation(program, 3, "inTexCoord");
	      
	    glLinkProgram(program); 
	       
	    int linked[] = {0};  
	    glGetProgramiv(program, GL_LINK_STATUS, linked);
	    if ( linked[0] == 0 ) 
	    {
	    	//Calculamos una cadena de error
	    	glGetProgramInfoLog(program);
	    	glDeleteProgram(program);
	    	program=0;
	    	return;
	    }	
	      
	  
	    //Uniformes
	    uProy		 = glGetUniformLocation(program, "proy");
	    //uModelView = glGetUniformLocation(program, "modelView");
	    uModel = glGetUniformLocation(program, "model");
	    uView = glGetUniformLocation(program, "view");
	    
	    uColorTex = glGetUniformLocation(program, "colorTex");
	    uEmiTex = glGetUniformLocation(program, "emiTex");
	    
	    uBrillo = glGetUniformLocation(program, "brillo");
		  
	    //Atributos
	    inPos = 0;
	    inColor = 1;
	    inNormal = 2;
	    inTexCoord = 3;
	}
	
	void sceneInit(){ 
		
		//ConfiguraciÃ³n del pipeline
		//hace el test de profundidad, se tiene que desactivar para hacer blend trasparencias
		glEnable(GL_DEPTH_TEST);
		
		//con que color vamos a rellenar cuando llamemos a limpiar
		glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
		
		//construimos la matriz de proyeccion
		MatrixMaths.buildProjectionMatrix(proy,45.0f,1.0f,0.1f,50.0f);
		
		//manera de rellenar los triangulos, como se hace la rasterizacion
		//rasteriza las dos caras, rellena el triangulo (los puntos internos del triangulo
		//podriamos tener GL_LINE (solo aristas) o GL_POINT (solo vertices)
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		//renderiza solo las caras que veas
		glEnable(GL_CULL_FACE);
		
		//subir la geometria
		//VAO, contenedor grande
		vao = glGenVertexArrays();
		glBindVertexArray(vao); //ACTIVAR: operaciones de ahora van a ESTE vao

		//Buffers
		posVBO = glGenBuffers( );
		colorVBO = glGenBuffers( );
		normalVBO = glGenBuffers( );
		texCoordVBO = glGenBuffers( );
		triangleIndexVBO = glGenBuffers( );

		glBindBuffer(GL_ARRAY_BUFFER, posVBO);
		//gl_static_draw es el uso comun de este buffer, contenido estatico
		glBufferData(GL_ARRAY_BUFFER, Cube.cubeVertexPos, GL_STATIC_DRAW);
		//punto entrada, tres componentes, tipo float (vec3), desde el punto 0, etc. 
		glVertexAttribPointer(inPos,3, GL_FLOAT,false, 0, 0); 
	    //habilitamos la entrada
		glEnableVertexAttribArray(inPos);

		glBindBuffer(GL_ARRAY_BUFFER, colorVBO);  
		glBufferData(GL_ARRAY_BUFFER, Cube.cubeVertexColor, GL_STATIC_DRAW);
	    glVertexAttribPointer(inColor,3, GL_FLOAT,false, 0, 0); 
	    glEnableVertexAttribArray(inColor);

		glBindBuffer(GL_ARRAY_BUFFER, normalVBO);  
		glBufferData(GL_ARRAY_BUFFER, Cube.cubeVertexNormal, GL_STATIC_DRAW);
	    glVertexAttribPointer(inNormal,3, GL_FLOAT,false, 0, 0); 
	    glEnableVertexAttribArray(inNormal);
	    
	    //buffer texturas tiene dos componentes (vec2)
		glBindBuffer(GL_ARRAY_BUFFER, texCoordVBO);  
		glBufferData(GL_ARRAY_BUFFER, Cube.cubeVertexTexCoord, GL_STATIC_DRAW);
	    glVertexAttribPointer(inTexCoord,2, GL_FLOAT,false, 0, 0); 
	    glEnableVertexAttribArray(inTexCoord);
		
	    //buffer de los indices
	    //punto entrada etapa ensamblado, buffer de elementos
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, triangleIndexVBO); 
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, Cube.cubeTriangleIndex, GL_STATIC_DRAW);
		
		//desbindeamos, para no trabajar con ninguno
	    glBindVertexArray(0);
	    
	    //cargamos la textura desde archivo
	    //array de bytes que va a ser la textura
	    //es IntBuffer porque es un paso por referencia, no un valor
	    IntBuffer wBuffer = MemoryStack.stackMallocInt(1);
	    IntBuffer hBuffer = MemoryStack.stackMallocInt(1);
	    //por que canales esta compuesto, rgb, rgba
	    IntBuffer composition = MemoryStack.stackMallocInt(1);
	    //pongo 3 canales, no necesitamos el canal alfa, que serian 4
	    ByteBuffer imagen = STBImage.stbi_load("src/caja.png", wBuffer, hBuffer, composition, 3);
	    
	    //convertimos a int
	    int width = wBuffer.get();
	    int height = hBuffer.get();
	    
	    ByteBuffer buffer = imagen.asReadOnlyBuffer();
	    
	    colorID = glGenTextures(); //reserva espacio memoria gráfica para la text
	    glBindTexture(GL_TEXTURE_2D, colorID);
	    //nivel 0 mayor resolucion, un byte por canal, 
	    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGB,
	    		GL_UNSIGNED_BYTE, buffer);
	    //definimos el tipo de interpolacion que vamos a hacer para la coord de la textura
	    //GL_LINEAR es posible (que se vaya al punto mas cercano)
	    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	    glGenerateMipmap(GL_TEXTURE_2D);
	}
	
	void render( )
	{	
		//limpiar el buffer de color y de profundidad
		glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
		
		//cargamos el programa (shader vertices, fragmentos, entradas
		glUseProgram(program);
		//subimos los uniform, subimos una matriz 4x4 de float
		//glUniformMatrix4fv(uModelView,false,modelView);
		glUniformMatrix4fv(uModel,false,Cube.model);
		glUniformMatrix4fv(uView,false,view);
		
		glUniformMatrix4fv(uProy,false,proy);
		glUniform1fv(uBrillo, brillo);
		//enlazamos el vao a renderizar
		glBindVertexArray(vao);
		
		//vec3 LPos;
		//glUniform3f(uLPos, v0, v1, v2);
		//glUniform3fv(ulPos,  value);
		
		glActiveTexture(GL_TEXTURE0); //el primer sampler definido, el 0
		glBindTexture(GL_TEXTURE_2D, colorID);
		glUniform1i(uColorTex, 0); //el sampler va a tomar el punto entrada 0
		
		//gl_triangles (tipo de primitiva, numero triangulos a pintar, ..., desde cual empiezo a pintar, tambien hay un rango)
		glDrawElements(GL_TRIANGLES, Cube.cubeNTriangleIndex, GL_UNSIGNED_INT, 0 );
		//glDrawElements(GL_TRIANGLES, 0, GL_UNSIGNED_INT, 0 );
		glBindVertexArray(0);
		glUseProgram(0);
	}
	void idle( )
	{
		angle += 0.00005;
		//MatrixMaths.loadIdentity(modelView);
		//MatrixMaths.translate(modelView,0.0f,0.0f,-6.0f);

		//MatrixMaths.rotateX(modelView,angle);
		//MatrixMaths.rotateY(modelView,angle);
		MatrixMaths.rotateX(Cube.model,angle);
		MatrixMaths.rotateY(Cube.model,angle);
	}
	
	public void run() {
		System.out.println("Practica:\nLWJGL version: " + Version.getVersion());
		init();
		//MatrixMaths.translate(modelView,0.0f,0.0f,-6.0f);
		MatrixMaths.translate(Cube.model,0.0f,0.0f,-6.0f);
		loop();
		
		//destroy();
		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(600, 600, "Hello World!", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");


		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.		
		glfwSetKeyCallback(window, keyCallback);
		// Setup a mouse button callback. It will be called every time a mouse button is pressed, repeated or released.		
		glfwSetMouseButtonCallback(window, mouseCallback );

		
		// Make the window visible
		glfwShowWindow(window);
		
		GL.createCapabilities();
		
		shaderInit();
		sceneInit();
	}
	
	private GLFWKeyCallbackI keyCallback = 
			( long window, int key, int scancode, int action, int mods ) ->
	{
		angle = 0.05f;
		if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE  )
			glfwSetWindowShouldClose(window, true);
		if ( key == GLFW_KEY_O && ( action == GLFW_PRESS || action == GLFW_REPEAT ))
		{
			System.out.println( "Key o pressed ");
			brillo[0] *= 2.0f;
		}
		if ( key == GLFW_KEY_L && ( action == GLFW_PRESS || action == GLFW_REPEAT ))
		{
			System.out.println( "Key l pressed ");
			brillo[0] *= 0.1f;
		}
		if ( key == GLFW_KEY_W && ( action == GLFW_PRESS || action == GLFW_REPEAT ))
		{
			System.out.println( "Key w pressed ");
			desplaza(0.0f, 0.0f, 0.1f);
		}
		if ( key == GLFW_KEY_S && ( action == GLFW_PRESS || action == GLFW_REPEAT ))
		{
			System.out.println( "Key s pressed ");
			desplaza(0.0f, 0.0f, -0.1f);
		}
		if ( key == GLFW_KEY_A && ( action == GLFW_PRESS || action == GLFW_REPEAT ))
		{
			System.out.println( "Key a pressed ");
			desplaza(0.1f, 0.0f, 0.0f);
			//MatrixMaths.rotateY(view,angle);
		}
		if ( key == GLFW_KEY_D && ( action == GLFW_PRESS || action == GLFW_REPEAT ))
		{
			System.out.println( "Key d pressed ");
			desplaza(-0.1f, 0.0f, 0.0f);
			//MatrixMaths.rotateY(view,-angle);
		}
		
	};
	
	private GLFWMouseButtonCallbackI mouseCallback = 
			( long window, int button, int action, int mods ) ->
	{
		if ( button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS )
		{
			System.out.println( "Mouse button 1 pressed" );
			double[] xpos = { 0.0 };
			double[] ypos = { 0.0 };
			glfwGetCursorPos(window, xpos, ypos);
			System.out.println( "Mouse position x: " + (float)xpos[0] + " y: " + (float)ypos[0] );
		}
		if ( button == GLFW_MOUSE_BUTTON_1 && action == GLFW_REPEAT )
		{
			System.out.println( "Mouse button 1 repeat" );
		}
	};

	private void loop() {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while ( !glfwWindowShouldClose(window) ) {
			
			//idle( );

			render();
		
			glfwSwapBuffers(window); // swap the color buffers

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}
	}
	
	private void desplaza(float x, float y, float z) {
		//MatrixMaths.loadIdentity(modelView);
		MatrixMaths.translate(view,x,y,z);
//		MatrixMaths.loadIdentity(proy);
//		MatrixMaths.translate(proy,x,y,z);

		//MatrixMaths.rotateX(modelView,angle);
		//MatrixMaths.rotateY(modelView,angle);
	}

	public static void main(String[] args) {
		new Main().run();
	}

}

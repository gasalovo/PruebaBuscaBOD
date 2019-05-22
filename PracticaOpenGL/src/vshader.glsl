#version 330

in vec3 inPos;
in vec3 inColor; //atributo de entrada para cada vertice
in vec3 inNormal;
in vec2 inTexCoord;

uniform mat4 proy;
//uniform mat4 modelView; //matriz producto de model y view
uniform mat4 model;
uniform mat4 view;

out vec3 vColor;
out vec3 position;
out vec3 N;
out vec2 vTexCoord;


void main() {
	mat4 modelView = (model * view);
	
	//el color en vertice no lo tocamos
	vColor = inColor;
	
	//posicion en el sistema ref de la camara
	position = (modelView * vec4(inPos, 1.0)).xyz;
	
	//la normal la calculamos en vertices
	//N = normalize(((transpose(inverse(modelView))) * vec4(inNormal,0.0))).xyz;
	N = ((transpose(inverse(modelView))) * vec4(inNormal,0.0)).xyz;
	
	//paso de valor
	vTexCoord = inTexCoord;
	
	//obligatorio, hay que darle el punto proyectado al rasterizador
	gl_Position = proy * modelView * vec4(inPos, 1.0); //el orden importa, un vector de 3 y la ultima coordenada un 1
	
}
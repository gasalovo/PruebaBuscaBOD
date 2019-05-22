#version 330

in vec3 vColor; //entrada que viene de vshader
in vec3 position; //posicion desde el sistema de referencia
in vec3 N;
in vec2 vTexCoord;

out vec4 color; //salida de fshader

vec3 ka;
vec3 kd;
vec3 ke;	

//CONSTANTES DE LA LUZ
vec3 Ia = vec3(0.2, 0.2, 0.2); //aportacion ambiental constante
// si la ponemos a 0 esta en el punto de vista de la camara

vec3 posL = vec3(1.0, 1.0, 1.0); //posicion luz
vec3 Il = vec3(1.0, 1.0, 1.0); //intensidad luz
//uniform float brillo = 100.0;

uniform float brillo;
uniform sampler2D colorTex; //tipo necesario para textura 2D

void main() {
	
	// si multiplico por dos vTexCoord, hago clamp y replico
	//ka = texture(colorTex, vTexCoord * 2.0f).rgb; //le pasamos un sample y una coord
	ka = texture(colorTex, vTexCoord * 2.0f).rgb;
	//ka = texture(colorTex, vTexCoord).rgb + vColor; //mezclijo con color
	kd = ka;
	ke = vec3(1.0);
	
	vec3 colorFinal = Ia * ka; //intensidad ambiental x color
	
	//DIFUSA
	//el N lo hemos calculado en vertices
	//calculamos vector entre nuestro punto y la luz
	vec3 L = normalize(posL - position); //normalize para que no afecte el modulo, y lo deja en 1
	
	//vector V es vector del objeto a la camara
	// posicion de la camara es 0 y posicion del objeto es p, por tanto el vector:
	vec3 V = normalize(-position); //el punto ref (la camara) esta en 0,0,0
	
	//ESPECULAR
	vec3 R = normalize(reflect(-L,N)); //rayo reflejado
	
	
	//difusa
	colorFinal += Il * kd * max(dot(N,L), 0.0); //dot producto escalar
	
	//especular
	colorFinal += Il * ke * max(pow(dot(R,V),brillo), 0.0);
	
	color = vec4(colorFinal, 1.0);
}
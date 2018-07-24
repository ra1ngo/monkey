var canvas = document.getElementById("canvas");
var gl = canvas.getContext("webgl");
if (!gl) {
    // у вас не работает webgl!
    alert('webgl не работает');
}

gl.enable(gl.DEPTH_TEST);	// включает использование буфера глубины
gl.depthFunc(gl.LEQUAL);	// определяет работу буфера глубины: более ближние объекты перекрывают дальние

//вьюпорт
gl.viewport(0, 0, canvas.width, canvas.height);

/*
//vao поддерживает в 1 версии webgl через расширение
ext = gl.getExtension("OES_vertex_array_object");
var vao = ext.createVertexArrayOES();  
//var vao = gl.createVertexArray();
//gl.bindVertexArray(vao);
ext.bindVertexArrayOES(vao);
*/


var shader_vertex_source="\n\
attribute vec4  vPosition; \n\
void main() \n\
{\n\
	gl_Position=vPosition;\n\
}";

var shader_fragment_source="\n\
precision mediump float;\n\
uniform vec4 vColor;\n\
void main()\n\
{\n\
	gl_FragColor=vColor;\n\
}";

//////////////////////////
function createShader(gl, type, source) {
	var shader = gl.createShader(type);   // создание шейдера
	gl.shaderSource(shader, source);      // устанавливаем шейдеру его программный код
	gl.compileShader(shader);             // компилируем шейдер
	return shader;
}
 
var vertexShader = createShader(gl, gl.VERTEX_SHADER, shader_vertex_source);
var fragmentShader = createShader(gl, gl.FRAGMENT_SHADER, shader_fragment_source);

var shaderProgram = gl.createProgram();
gl.attachShader(shaderProgram, vertexShader);
gl.attachShader(shaderProgram, fragmentShader);
gl.linkProgram(shaderProgram);
gl.useProgram(shaderProgram);
////////////////////////


//массив вершин треугольника
var vertices = [
    -0.5, -0.5, 0.0,
    0.5, -0.5, 0.0,
    0.0,  0.5, 0.0
];

//объекты вершинного буфера (vertex buffer objects)
var VBO = gl.createBuffer();
gl.bindBuffer(gl.ARRAY_BUFFER, VBO);

// Передадим информацию о вершинах в OpenGL
gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vertices), gl.STATIC_DRAW);

// Разрешаем обработку вершин треугольника
gl.enableVertexAttribArray(0);
gl.vertexAttribPointer(0, 3, gl.FLOAT, false, 0, 0);

function loop(){

	// очищаем canvas
  	gl.clearColor(0.2, 0.3, 0.3, 1.0);
  	gl.clear(gl.COLOR_BUFFER_BIT);

  	gl.drawArrays(gl.TRIANGLES, 0, 3);

  	requestAnimationFrame(loop);
};

loop();
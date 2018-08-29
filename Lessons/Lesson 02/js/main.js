var canvas = document.getElementById("canvas");
var gl = canvas.getContext("webgl");
if (!gl) {
    // � ��� �� �������� webgl!
    alert('webgl �� ��������');
}

gl.enable(gl.DEPTH_TEST);	// �������� ������������� ������ �������
gl.depthFunc(gl.LEQUAL);	// ���������� ������ ������ �������: ����� ������� ������� ����������� �������

//�������
gl.viewport(0, 0, canvas.width, canvas.height);

/*
//vao ������������ � 1 ������ webgl ����� ����������
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
	var shader = gl.createShader(type);   // �������� �������
	gl.shaderSource(shader, source);      // ������������� ������� ��� ����������� ���
	gl.compileShader(shader);             // ����������� ������
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


//������ ������ ������������
var vertices = [
    -0.5, -0.5, 0.0,
    0.5, -0.5, 0.0,
    0.0,  0.5, 0.0
];

//������� ���������� ������ (vertex buffer objects)
var VBO = gl.createBuffer();
gl.bindBuffer(gl.ARRAY_BUFFER, VBO);

// ��������� ���������� � �������� � OpenGL
gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vertices), gl.STATIC_DRAW);

// ��������� ��������� ������ ������������
gl.enableVertexAttribArray(0);
gl.vertexAttribPointer(0, 3, gl.FLOAT, false, 0, 0);

function loop(){

	// ������� canvas
  	gl.clearColor(0.2, 0.3, 0.3, 1.0);
  	gl.clear(gl.COLOR_BUFFER_BIT);

  	gl.drawArrays(gl.TRIANGLES, 0, 3);

  	requestAnimationFrame(loop);
};

loop();
var canvas = document.getElementById("canvas");
var gl = canvas.getContext("webgl");
if (!gl) {
    // � ��� �� �������� webgl!
    alert('webgl �� ��������');
}

gl.enable(gl.DEPTH_TEST);	// �������� ������������� ������ �������
gl.depthFunc(gl.LEQUAL);	// ���������� ������ ������ �������: ����� ������� ������� ����������� �������
    

gl.viewportWidth = canvas.width;
gl.viewportHeight = canvas.height;

gl.viewport(0, 0, gl.viewportWidth, gl.viewportHeight);

function loop(){

	// ������� canvas
  	gl.clearColor(0.2, 0.3, 0.3, 1.0);
  	gl.clear(gl.COLOR_BUFFER_BIT);

  	loop();
};

loop();
var canvas = document.getElementById("canvas");
var gl = canvas.getContext("webgl");
if (!gl) {
    // у вас не работает webgl!
    alert('webgl не работает');
}

gl.enable(gl.DEPTH_TEST);	// включает использование буфера глубины
gl.depthFunc(gl.LEQUAL);	// определяет работу буфера глубины: более ближние объекты перекрывают дальние
    

gl.viewportWidth = canvas.width;
gl.viewportHeight = canvas.height;

gl.viewport(0, 0, gl.viewportWidth, gl.viewportHeight);

function loop(){

	// очищаем canvas
  	gl.clearColor(0.2, 0.3, 0.3, 1.0);
  	gl.clear(gl.COLOR_BUFFER_BIT);

  	loop();
};

loop();
function Monkey(){
	//приватные свойства
	//var _a;
	//var CONST;
	//приватные функции
	//var func - function(){}.bind(this);
	//public поля
	//this.canvas = document.getElementById("canvas");

	//приватные свойства
	var canvas = document.getElementById("canvas");
    var gl = canvas.getContext("webgl");
    //КОНСТРУКТОР
    if (!gl) {
        // у вас не работает webgl!
        alert('webgl не работает');
    }

    //public функции
    this.setScreen = function(s) {
	    //скрины на js пока не реализованы
	};

	this.clear = function() {
		gl.enable(gl.DEPTH_TEST); // включает использование буфера глубины
    	gl.depthFunc(gl.LEQUAL);  // определяет работу буфера глубины: более ближние объекты перекрывают дальние

	    gl.clearColor(0.2, 0.3, 0.3, 1.0);
    	gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
	};

///////////////////////////////////
//////////////КЛАССЫ///////////////
///////////////////////////////////	
	function Viewport(w,h){
		this.width = w;
		this.height = h;
		gl.viewport(0, 0, w, h);
	}
	this.createViewport = function (w,h) {
		return new Viewport(w,h);
	};

	//Окна пока до конца не реализованы
	/*
		Screen()
		Screen(Viewport v)
	*/
	function Screen(v){
		this.context = 0;

		//перегрузка конструкторов
		if(typeof v == "undefined") {
			//Конуструктор Screen()
			this.viewport = new Viewport(canvas.width, canvas.height);
		} else {
			//Конуструктор Screen(v)
			this.viewport = v; 
		}
	}
	this.createScreen = function (v) {
		return new Screen(v);
	};





	//текстура
	function Texture(p, zBuffer, a){
        this.path = p;
        this.z = zBuffer;
        this.alpha = a;

        this.img = new Image();
		this.img.src = this.path;
		this.init = false;
		this.img.onload = function() {
		    this.shader = new this.Shader();
        	this.mesh = new this.Mesh(this.shader.shaderProgram, this.img);
        	this.init = true;
		}.bind(this);

////////////////////////////////////////////////////////////////////
        //затем убрать это
        var MVP = mat4.create();


        var position = vec3.fromValues(0.0, 0.5, 0.0);
        var rotation = vec3.fromValues(0.0, 0.0, 1.0);
        var scale    = vec3.fromValues(0.5, 0.5, 1.0);


        var trans = mat4.create();
        mat4.identity(trans);


        var angle = 90;

        mat4.translate(trans, trans, position);
        mat4.rotate(trans, trans, glMatrix.toRadian(angle), rotation);
        mat4.scale(trans, trans, scale);




        var zoom = 1.0;
        var cameraPosition = vec3.fromValues(0.0, 0.5, 10.0);
        var cameraTarget   = vec3.fromValues(0.0, 0.5, 0.0);
        var upVector       = vec3.fromValues(0.0, 1.0, 0.0);

        var projection = mat4.create();
        mat4.ortho(projection, -1.0 * zoom, 1.0 * zoom, -1.0 * zoom, 1.0 * zoom, 0.1, 20.0);

        var CameraMatrix =  mat4.create();
        mat4.lookAt(CameraMatrix, cameraPosition, cameraTarget, upVector);

        zoom = 0.75;
        mat4.ortho(projection, -1.0 * zoom, 1.0 * zoom, -1.0 * zoom, 1.0 * zoom, 0.1, 20.0);

        //var VP = mat4.create();
        //mat4.multiply(VP, projection, CameraMatrix);

        MVP = mat4.create();
        //mat4.multiply(MVP, VP, trans);
        mat4.multiply(MVP, CameraMatrix, trans);
        mat4.multiply(MVP, projection, MVP);
////////////////////////////////////////////////////////////////////

		this.draw = function(zBuffer){
			gl.enable(gl.BLEND);
			gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);

			if (this.init){
				this.shader.use();

				var alphaLoc = gl.getUniformLocation(this.shader.shaderProgram, "alpha");
				gl.uniform1f(alphaLoc, this.alpha);

				var MVPLoc = gl.getUniformLocation(this.shader.shaderProgram, "MVP");
				gl.uniformMatrix4fv(MVPLoc, false, MVP);

				var zLoc = gl.getUniformLocation(this.shader.shaderProgram, "z");
				//перегрузка функций
				if(arguments.length==0) {
					//вызов draw()
					gl.uniform1f(zLoc, this.z);
				} else if (arguments.length==1){
					//вызов draw(zBuffer)
					gl.uniform1f(zLoc, zBuffer);
				}

				
				this.mesh.draw();
			}

			gl.disable(gl.BLEND);
		}
	}



	Texture.prototype.Shader = function(){
		//private
		var vertexShader;
        var fragmentShader;

        
        function createShader(type, source) {
            var shader = gl.createShader(type);   // создание шейдера
            gl.shaderSource(shader, source);      // устанавливаем шейдеру его программный код
            gl.compileShader(shader);             // компилируем шейдер

            // Проверить успешное завершение компиляции
            if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {  
                alert("Не скомпилировался шейдер: " + source + "\n\n" + gl.getShaderInfoLog(shader));  
                return null;  
            }
            return shader;
        }
        //////////////////////////
        //public
        this.vertex=`
	    attribute vec3 position;
	    attribute vec3 color;
	    attribute vec2 texCoord;
	    varying vec3 vertexColor;
	    varying vec2 TexCoord;
	    uniform mat4 MVP;
	    uniform float z;
	    void main()
	    {
	       	gl_Position = MVP * vec4(position.x, position.y, z, 1.0);
	       	vertexColor = color;
	    	TexCoord = vec2(texCoord.x, 1.0 - texCoord.y);
	    }`;
        this.fragment=`
	    precision mediump float;
	    varying vec3 vertexColor;
	    varying vec2 TexCoord;
	    uniform float alpha;
	    uniform sampler2D ourTexture;
	    void main()
	    {
			vec4 texColor = texture2D(ourTexture, TexCoord);
			if(texColor.a < 0.1) discard;
			texColor.a = alpha;
	      	gl_FragColor = texColor;
	    }`;

	    vertexShader = createShader(gl.VERTEX_SHADER, this.vertex);
	    fragmentShader = createShader(gl.FRAGMENT_SHADER, this.fragment);

	    this.shaderProgram = gl.createProgram();
	    gl.attachShader(this.shaderProgram, vertexShader);
	    gl.attachShader(this.shaderProgram, fragmentShader);
	    gl.linkProgram(this.shaderProgram);

	    if (!gl.getProgramParameter(this.shaderProgram, gl.LINK_STATUS)) {
	        alert("Не слинковалась шейдерная программа: \n" + gl.getProgramInfoLog(shaderProgram));
	    }

	    this.use = function() { gl.useProgram(this.shaderProgram); }
	}



	Texture.prototype.Mesh = function(s, i){

		//массив вершин треугольника
		this.vertices = [
		    // Позиции 
		     0.5,  0.5, 0.0, // Верхний правый угол
		     0.5, -0.5, 0.0, // Нижний правый угол
		    -0.5, -0.5, 0.0, // Нижний левый угол
		    -0.5,  0.5, 0.0  // Верхний левый угол
		];
		this.indices = [
		    0, 1, 3,   // Первый треугольник
		    1, 2, 3    // Второй треугольник
		];
		this.colors = [
		    // Цвета
		    1.0, 0.0, 0.0, // Верхний правый угол
		    0.0, 1.0, 0.0, // Нижний правый угол
		    0.0, 0.0, 1.0, // Нижний левый угол
		    1.0, 0.0, 1.0  // Верхний левый угол
		];
		this.texCoords = [
		    1.0, 1.0,     // Верхний правый угол
		    1.0, 0.0,     // Нижний правый угол
		    0.0, 0.0,     // Нижний левый угол
		    0.0, 1.0      // Верхний левый угол
		];

		this.img = i;

		var shaderProgram = s;

		var texture = gl.createTexture();
		gl.bindTexture(gl.TEXTURE_2D, texture);
		//gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, true);
		gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, this.img);
		gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
		gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
		gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
		gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
		gl.bindTexture(gl.TEXTURE_2D, null);



		//объекты вершинного буфера (vertex buffer objects)
		var VBO = gl.createBuffer();
		gl.bindBuffer(gl.ARRAY_BUFFER, VBO);
		// Передадим информацию о вершинах в OpenGL
		gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(this.vertices), gl.STATIC_DRAW);

		//цвета
		var colorBO = gl.createBuffer();
		gl.bindBuffer(gl.ARRAY_BUFFER, colorBO);
		// Передадим информацию о вершинах в OpenGL
		gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(this.colors), gl.STATIC_DRAW);

		//текстурные координаты
		var texBO = gl.createBuffer();
		gl.bindBuffer(gl.ARRAY_BUFFER, texBO);
		gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(this.texCoords), gl.STATIC_DRAW);

		//инфа о индексах
		var IBO = gl.createBuffer();
		gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, IBO);
		gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(this.indices), gl.STATIC_DRAW);

		this.draw = function(){
			gl.bindBuffer(gl.ARRAY_BUFFER, VBO);
			posLoc = gl.getAttribLocation(shaderProgram, "position");
			gl.vertexAttribPointer(posLoc, 3, gl.FLOAT, false, 0, 0);
			gl.enableVertexAttribArray(posLoc);
			gl.bindBuffer(gl.ARRAY_BUFFER, colorBO);
			colorLoc = gl.getAttribLocation(shaderProgram, "color");
			gl.vertexAttribPointer(colorLoc, 3, gl.FLOAT, false, 0, 0);
			gl.enableVertexAttribArray(colorLoc);
			gl.bindBuffer(gl.ARRAY_BUFFER, texBO);
			texLoc = gl.getAttribLocation(shaderProgram, "texCoord");
			gl.vertexAttribPointer(texLoc, 2, gl.FLOAT, false, 0, 0);
			gl.enableVertexAttribArray(texLoc);


			//gl.activeTexture(gl.TEXTURE0);
			gl.bindTexture(gl.TEXTURE_2D, texture);
			//var samplerUniform = gl.getUniformLocation(shaderProgram, "ourTexture");
			//gl.uniform1i(samplerUniform, 0);

			gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, IBO);
			gl.drawElements(gl.TRIANGLES, 6, gl.UNSIGNED_SHORT, 0);
		}

	}

	this.createTexture = function (p, zBuffer, a) {
		return new Texture(p, zBuffer, a);
	};

}
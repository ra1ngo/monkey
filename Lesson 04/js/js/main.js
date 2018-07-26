//если есть сервер или локалка
//без сервака не обойти CORS
//или добавить --allow-file-access-from-files
var img = new Image();
img.src = "./img/elli_walk.png";
img.onload = function() {
    main(img);
}

function main(img){
    var canvas = document.getElementById("canvas");
    var gl = canvas.getContext("webgl");
    if (!gl) {
        // у вас не работает webgl!
        alert('webgl не работает');
    }


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


    var shader_vertex_source=`
    attribute vec3 position;
    attribute vec3 color;
    attribute vec2 texCoord;
    varying vec3 vertexColor;
    varying vec2 TexCoord;
    void main()
    {
       gl_Position = vec4(position, 1.0);
       vertexColor = color;
       TexCoord = texCoord;
    }`;


    var shader_fragment_source=`
    precision mediump float;
    varying vec3 vertexColor;
    varying vec2 TexCoord;
    uniform float alpha;
    uniform sampler2D ourTexture;
    void main()
    {
       gl_FragColor = texture2D(ourTexture, TexCoord);
    }`;

    //////////////////////////
    function createShader(gl, type, source) {
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
     
    var vertexShader = createShader(gl, gl.VERTEX_SHADER, shader_vertex_source);
    var fragmentShader = createShader(gl, gl.FRAGMENT_SHADER, shader_fragment_source);

    var shaderProgram = gl.createProgram();
    gl.attachShader(shaderProgram, vertexShader);
    gl.attachShader(shaderProgram, fragmentShader);
    gl.linkProgram(shaderProgram);

    if (!gl.getProgramParameter(shaderProgram, gl.LINK_STATUS)) {
        alert("Не слинковалась шейдерная программа: \n" + gl.getProgramInfoLog(shaderProgram));
    }
    ////////////////////////


    //массив вершин треугольника
    var vertices = [
        // Позиции 
         0.5,  0.5, 0.0, // Верхний правый угол
         0.5, -0.5, 0.0, // Нижний правый угол
        -0.5, -0.5, 0.0, // Нижний левый угол
        -0.5,  0.5, 0.0  // Верхний левый угол
    ];
    var indices = [
        0, 1, 3,   // Первый треугольник
        1, 2, 3    // Второй треугольник
    ];
    var colors = [
        // Цвета
        1.0, 0.0, 0.0, // Верхний правый угол
        0.0, 1.0, 0.0, // Нижний правый угол
        0.0, 0.0, 1.0, // Нижний левый угол
        1.0, 0.0, 1.0  // Верхний левый угол
    ];
    var texCoords = [
        1.0, 1.0,     // Верхний правый угол
        1.0, 0.0,     // Нижний правый угол
        0.0, 0.0,     // Нижний левый угол
        0.0, 1.0      // Верхний левый угол
    ];



    var texture = gl.createTexture();
    gl.bindTexture(gl.TEXTURE_2D, texture);
    gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, true);
    gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, img);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
    gl.bindTexture(gl.TEXTURE_2D, null);





    //объекты вершинного буфера (vertex buffer objects)
    var VBO = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, VBO);
    // Передадим информацию о вершинах в OpenGL
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vertices), gl.STATIC_DRAW);

    //цвета
    var colorBO = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, colorBO);
    // Передадим информацию о вершинах в OpenGL
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(colors), gl.STATIC_DRAW);

    //текстурные координаты
    var texBO = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, texBO);
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(texCoords), gl.STATIC_DRAW);

    //инфа о индексах
    var IBO = gl.createBuffer();
    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, IBO);
    gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(indices), gl.STATIC_DRAW);

    function loop(){

        // очищаем canvas
        gl.clearColor(0.2, 0.3, 0.3, 1.0);
        gl.clear(gl.COLOR_BUFFER_BIT);

        gl.enable(gl.DEPTH_TEST); // включает использование буфера глубины
        gl.depthFunc(gl.LEQUAL);  // определяет работу буфера глубины: более ближние объекты перекрывают дальние

        gl.enable(gl.BLEND);
        gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);


        var alphaLocation = gl.getUniformLocation(shaderProgram, "alpha");
        gl.useProgram(shaderProgram);
        gl.uniform1f(alphaLocation, 0.5);

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


        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, texture);
        var samplerUniform = gl.getUniformLocation(shaderProgram, "ourTexture");
        gl.uniform1i(samplerUniform, 0);

        gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, IBO);
        gl.drawElements(gl.TRIANGLES, 6, gl.UNSIGNED_SHORT, 0);

        gl.disable(gl.BLEND);

        requestAnimationFrame(loop);
    };

    loop();
}
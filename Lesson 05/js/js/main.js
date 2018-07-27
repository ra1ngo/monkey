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
    uniform mat4 transform;
    void main()
    {
       gl_Position = transform * vec4(position, 1.0);
       vertexColor = color;
       TexCoord = vec2(texCoord.x, 1.0 - texCoord.y);
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
    //gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, true);
    gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, img);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
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







    function toEuler(w, x, y, z){
     
        var heading, attitude, bank;

        var test = x*y + z*w;
        if (test > 0.499) { // singularity at north pole
            heading = 2 * Math.atan2(x,w);
            attitude = Math.PI/2;
            bank = 0;
        }
        if (test < -0.499) { // singularity at south pole
            heading = -2 * Math.atan2(x,w);
            attitude = - Math.PI/2;
            bank = 0;
        }
        if(isNaN(heading)){
            var sqx = x*x;
            var sqy = y*y;
            var sqz = z*z;
            heading = Math.atan2(2*y*w - 2*x*z , 1 - 2*sqy - 2*sqz);
            attitude = Math.asin(2*test);
            bank = Math.atan2(2*x*w - 2*y*z , 1 - 2*sqx - 2*sqz);
        }

        console.log("bank: " + (180+bank*180/Math.PI) + " heading: " + heading*180/Math.PI + " attitude: " + attitude*180/Math.PI);
        return vec3.fromValues(bank, heading, attitude);
    };




    var position = vec3.fromValues(0.0, 0.5, 0.0);
    var rotation = vec3.fromValues(0.0, 0.0, 1.0);
    var scale    = vec3.fromValues(0.5, 0.5, 1.0);


    var trans = mat4.create();
    mat4.identity(trans);


    var angle = 90;

    mat4.translate(trans, trans, position);
    mat4.rotate(trans, trans, glMatrix.toRadian(angle), rotation);
    mat4.scale(trans, trans, scale);


    var q = quat.create();
    var pos = vec3.create();
    var rot = vec3.create();
    var skl = vec3.create();
    mat4.getTranslation(pos, trans);
    //console.log(trans);
    console.log("pos.x: " + pos[0] + " pos.y: " + pos[1] + " pos.z: " + pos[2]);
    mat4.getScaling(skl, trans);
    console.log("skl.x: " + skl[0] + " skl.y: " + skl[1] + " skl.z: " + skl[2]);
    mat4.getRotation(q, trans);
    console.log("quaternion: "+ q);
    toEuler(q[0], q[1], q[2], q[3]);

    /*
    function row(r){
        return vec3.fromValues(
            trans[r*4],
            trans[r*4+1],
            trans[r*4+2],
            trans[r*4+3]
        );
    }
    function extractEulerAngleXYZ(){
        var rotXangle = Math.atan2(-row(1)[2], row(2)[2]);
        var cosYangle = Math.sqrt(Math.pow(row(0)[0], 2) + Math.pow(row(0)[1], 2));
        var rotYangle = Math.atan2(row(0)[2], cosYangle);
        var sinXangle = Math.sin(rotXangle);
        var cosXangle = Math.cos(rotXangle);
        var rotZangle = Math.atan2(cosXangle * row(1)[0] + sinXangle * row(2)[0], cosXangle * row(1)[1]+ sinXangle * row(2)[1]);
        console.log("rotXangle " + (- rotXangle * 180 / Math.PI));
        console.log("rotYangle " + (- rotYangle * 180 / Math.PI));
        console.log("rotZangle " + (- rotZangle * 180 / Math.PI));
    }
    extractEulerAngleXYZ();
    */






    function loop(){

        // очищаем canvas
        gl.clearColor(0.2, 0.3, 0.3, 1.0);
        gl.clear(gl.COLOR_BUFFER_BIT);

        //gl.enable(gl.DEPTH_TEST); // включает использование буфера глубины
        //gl.depthFunc(gl.LEQUAL);  // определяет работу буфера глубины: более ближние объекты перекрывают дальние

        gl.enable(gl.BLEND);
        gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);

        gl.useProgram(shaderProgram);

        var alphaLoc = gl.getUniformLocation(shaderProgram, "alpha");
        gl.uniform1f(alphaLoc, 0.5);

        var transformLoc = gl.getUniformLocation(shaderProgram, "transform");
        gl.uniformMatrix4fv(transformLoc, false, trans);


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

        gl.disable(gl.BLEND);

        requestAnimationFrame(loop);
    };

    loop();
}
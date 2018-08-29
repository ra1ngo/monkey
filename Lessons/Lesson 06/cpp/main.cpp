// GLEW нужно подключать до GLFW.
// GLEW
//#define GLEW_STATIC
#include <GL/glew.h>
// GLFW
#include <GLFW/glfw3.h>

#define STB_IMAGE_IMPLEMENTATION
#include <STB/stb_image.h>

#include <GLM/glm.hpp>
#include <GLM/gtc/matrix_transform.hpp>
#include <GLM/gtc/type_ptr.hpp>
#define GLM_ENABLE_EXPERIMENTAL
#include <GLM/gtx/matrix_decompose.hpp>
// Включаем стандартные заголовки
#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <string>
#include <vector>


GLuint createShader(GLenum type, const char *source) {
    // Create the shader object
    GLuint shader = glCreateShader(type);
    // Load the shader source
    glShaderSource(shader, 1, &source, NULL);
    // Compile the shader
    glCompileShader(shader);


    GLint status;
    GLchar infoLog[512];
    glGetShaderiv(shader, GL_COMPILE_STATUS, &status);
    if(!status)
    {
        glGetShaderInfoLog(shader, 512, NULL, infoLog);
        std::cout << "ERROR::SHADER::COMPILATION_FAILED\n" << infoLog << std::endl;
    }

    return shader;
}
/*
//это неправильная функция, она не учитывает скейлинг по каждой оси.
void setPosition(glm::mat4 &m, glm::vec3 &v, float z){
    m[3][0] = v.x*z;
    m[3][1] = v.y*z;
    m[3][2] = v.z;
}
*/
int main(void){
    GLFWwindow* window;

    // Инициализируем GLFW
    if( !glfwInit() ){
        fprintf( stderr, "Ошибка при инициализации GLFWn" );
        return -1;
    }

    //glfwWindowHint(GLFW_FSAA_SAMPLES, 4); // 4x Сглаживание
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3); // Мы хотим использовать OpenGL 3.3
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE); // To make MacOS happy; should not be needed
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); // Мы не хотим старый OpenGL
	glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);    //Выключение возможности изменения размера окна

    // Открыть окно и создать в нем контекст OpenGL
    window = glfwCreateWindow(640, 480, "Hello World", NULL, NULL);
    //GLFWwindow* window = glfwCreateWindow(800, 600, "LearnOpenGL", null, null);
    if( window == NULL ){
        fprintf( stderr, "Невозможно открыть окно GLFW. Если у вас Intel GPU, то он не поддерживает версию 3.3. Попробуйте версию уроков для OpenGL 2.1.n" );
        glfwTerminate();
        return -1;
    }
    /* Make the window's context current */
    glfwMakeContextCurrent(window);

    // Инициализируем GLEW
    GLenum err = glewInit();
    if (GLEW_OK != err){
      /* Problem: glewInit failed, something is seriously wrong. */
      fprintf(stderr, "Error: %s\n", glewGetErrorString(err));

    }
    fprintf(stdout, "Status: Using GLEW %s\n", glewGetString(GLEW_VERSION));

    //Viewport
    int width, height;
    glfwGetFramebufferSize(window, &width, &height);
    glViewport(0, 0, width, height);

    // Включим режим отслеживания нажатия клавиш, для проверки ниже
    //glfwSetInputMode(window, GLFW_STICKY_KEYS, GL_TRUE);


    /////////////////
    /////ШЕЙДЕРЫ/////
    /////////////////
    std::string shader_vertex_source =
        "#version 330 core\n"
        "in vec3 position;\n"
        "in vec3 color;\n"
        "in vec2 texCoord;\n"
        "out vec3 vertexColor;\n"
        "out vec2 TexCoord;\n"
        "uniform mat4 MVP;\n"
        "void main()\n"
        "{\n"
        "   gl_Position = MVP * vec4(position, 1.0f);\n"
        "   vertexColor = color;\n"
        "   TexCoord = vec2(texCoord.x, 1.0 - texCoord.y);\n"
        "}\n";

    std::string shader_fragment_source =
        "#version 330 core\n"
        "in vec3 vertexColor;\n"
        "in vec2 TexCoord;\n"
        "out vec4 FragColor;\n"
        "uniform float alpha;\n"
        "uniform sampler2D ourTexture;\n"
        "void main()\n"
        "{\n"
        //"   FragColor=vec4(vertexColor, alpha);\n"
        "   FragColor = texture(ourTexture, TexCoord);"
        "}\n";
    //vec4(1, 0, 0.5, 1)


    const char *source;
    //int length;
    source = shader_vertex_source.c_str();
    //length = shader_vertex_source.size();
    GLuint vertexShader = createShader(GL_VERTEX_SHADER, source);
    //int length1;
    source = shader_fragment_source.c_str();
    //length1 = shader_fragment_source.size();
    GLuint fragmentShader = createShader(GL_FRAGMENT_SHADER, source);


    GLuint shaderProgram = glCreateProgram();
    glAttachShader(shaderProgram, vertexShader);
    glAttachShader(shaderProgram, fragmentShader);
    glLinkProgram(shaderProgram);

    //отладка
    GLint status;
    glGetProgramiv(shaderProgram, GL_LINK_STATUS, &status);
    if(status == GL_FALSE) {
        GLint length;
        glGetProgramiv(shaderProgram, GL_INFO_LOG_LENGTH, &length);
        std::vector<char> log(length);
        glGetProgramInfoLog(shaderProgram, length, &length, &log[0]);
        std::cerr << &log[0];
        return false;
    }

    //массив вершин прямоугольника
    GLfloat vertices[] = {
        // Позиции
         0.5f,  0.5f, 0.0f, // Верхний правый угол
         0.5f, -0.5f, 0.0f, // Нижний правый угол
        -0.5f, -0.5f, 0.0f, // Нижний левый угол
        -0.5f,  0.5f, 0.0f  // Верхний левый угол
    };
    GLfloat colors[] = {
        // Цвета
        1.0f, 0.0f, 0.0f, // Верхний правый угол
        0.0f, 1.0f, 0.0f, // Нижний правый угол
        0.0f, 0.0f, 1.0f, // Нижний левый угол
        1.0f, 0.0f, 1.0f // Верхний левый угол
    };
    GLuint indices[] = {  // Помните, что мы начинаем с 0!
        0, 1, 3,   // Первый треугольник
        1, 2, 3    // Второй треугольник
    };
    GLfloat texCoords[] = {
        1.0f, 1.0f,     // Верхний правый угол
        1.0f, 0.0f,      // Нижний правый угол
        0.0f, 0.0f,     // Нижний левый угол
        0.0f, 1.0f     // Верхний левый угол
    };

    int widthIMG, heightIMG, channels;
    //stbi_set_flip_vertically_on_load(true);
    //unsigned char *image = stbi_load("res/img.jpg",&widthIMG,&heightIMG,&channels, STBI_rgb);
    unsigned char *image = stbi_load("res/elli_walk.png",&widthIMG,&heightIMG,&channels, STBI_rgb_alpha);
    if(image == NULL) {
        std::cout << "loadTexture failed" << image << std::endl;
        return false;
    }

    GLuint texture;
    glGenTextures(1, &texture);
    glBindTexture(GL_TEXTURE_2D, texture);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, widthIMG, heightIMG, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);

    // Устанавливаем настройки фильтрации и преобразований (на текущей текстуре)
    //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glBindTexture(GL_TEXTURE_2D, 0);
    stbi_image_free(image);



    GLuint VBO;
    glGenBuffers(1, &VBO);
    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);

    GLuint colorBO;
    glGenBuffers(1, &colorBO);
    glBindBuffer(GL_ARRAY_BUFFER, colorBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(colors), colors, GL_STATIC_DRAW);

    GLuint texBO;
    glGenBuffers(1, &texBO);
    glBindBuffer(GL_ARRAY_BUFFER, texBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(texCoords), texCoords, GL_STATIC_DRAW);

    GLuint IBO;
    glGenBuffers(1, &IBO);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IBO);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(indices), indices, GL_STATIC_DRAW);















    glm::vec3 position = glm::vec3(0.0f, 0.5f, 0.0f);
    glm::vec3 rotation = glm::vec3(0.0f, 0.0f, 1.0f);
    glm::vec3 scale    = glm::vec3(0.5f, 0.5f, 1.0f);

    glm::mat4 trans = glm::mat4(1.0f);
    //glm::mat4 positionM = glm::mat4(1.0f);
    //glm::mat4 rotationM = glm::mat4(1.0f);
    //glm::mat4 scaleM = glm::mat4(1.0f);

    float angle = 90.0f;

    trans = glm::translate(trans, position);
    trans = glm::rotate(trans, glm::radians(angle), rotation);
    trans = glm::scale(trans, scale);

    //trans = positionM * rotationM * scaleM;


    //position
    /*
    glm::vec3 pos = trans[3];
    std::cout << "pos.x: " << pos.x << "\npos.y: "  << pos.y << "\npos.z: " <<  pos.z << std::endl;
    */


    glm::vec3 scl;
    glm::quat q;
    glm::vec3 pos;
    glm::vec3 skew;
    glm::vec4 perspective;
    glm::decompose(trans, scl, q, pos, skew, perspective);
    glm::vec3 rot = glm::eulerAngles(q);
    std::cout << "eulerAngles.x: " << rot.x << "\neulerAngles.y: "  << rot.y << "\neulerAngles.z: " <<  rot.z << std::endl;
    rot = glm::degrees(rot);
    std::cout << "pos.x: " << pos.x << "\npos.y: "  << pos.y << "\npos.z: " <<  pos.z << std::endl;
    std::cout << "scl.x: " << scl.x << "\nscl.y: "  << scl.y << "\nscl.z: " <<  scl.z << std::endl;
    std::cout << "rot.x: " << rot.x << "\nrot.y: "  << rot.y << "\nrot.z: " <<  rot.z << std::endl;




    /*
    glm::mat4 projection = glm::perspective(45.0f, //fov
                                            (float)width / (float)height, //4.0f / 3.0f
                                            0.1f,  // Ближняя плоскость отсечения. Должна быть больше 0.
                                            100.0f);  // Дальняя плоскость отсечения.
*/

    float zoom = 1.f;

    glm::vec3 cameraPosition = glm::vec3(0.f, 0.5f, 10.f);
    glm::vec3 cameraTarget = glm::vec3(0.0f, 0.5f, 0.0f);
    glm::vec3 upVector = glm::vec3(0.0f, 1.0f, 0.0f);

    glm::mat4 projection = glm::ortho(-1.f * zoom, 1.f * zoom, -1.f * zoom, 1.f * zoom, 0.1f, 20.f);

    glm::mat4 CameraMatrix = glm::lookAt(
        cameraPosition, // Позиция камеры в мировом пространстве
        cameraTarget,   // Указывает куда вы смотрите в мировом пространстве
        upVector        // Вектор, указывающий направление вверх. Обычно (0, 1, 0)
    );

    zoom = 0.75f;
    projection = glm::ortho(-1.f * zoom, 1.f * zoom, -1.f * zoom, 1.f * zoom, 0.1f, 20.f);

    glm::mat4 MVP = projection * CameraMatrix * trans;
    //glm::mat4 MVP = CameraMatrix * trans;     //не работает


/*
    float zoom = 1.5f;
    glm::vec3 cameraPosition = glm::vec3(0.f, -0.5f, 0.f);
    glm::vec3 cameraScale    = glm::vec3(zoom, zoom, 1.0f);

    glm::mat4 CameraMatrix = glm::mat4(1.0f);
    setPosition(CameraMatrix, cameraPosition, 1.f);

    std::cout << CameraMatrix[0][0] << CameraMatrix[1][0] << CameraMatrix[2][0] <<  CameraMatrix[3][0] <<std::endl;
    std::cout << CameraMatrix[0][1] << CameraMatrix[1][1] << CameraMatrix[2][1] <<  CameraMatrix[3][1] <<std::endl;
    std::cout << CameraMatrix[0][2] << CameraMatrix[1][2] << CameraMatrix[2][2] <<  CameraMatrix[3][2] <<std::endl;
    std::cout << CameraMatrix[0][3] << CameraMatrix[1][3] << CameraMatrix[2][3] <<  CameraMatrix[3][3] <<std::endl;


    CameraMatrix = glm::scale(CameraMatrix, cameraScale);
    setPosition(CameraMatrix, cameraPosition, zoom);    //после каждого зума нужно дополнительное передвижение
                                                                        //координаты изменились, но не обновились
    glm::mat4 MVP = CameraMatrix * trans;

*/






    //Vertex Array Object!!!
    GLuint VAO;
    glGenVertexArrays(1, &VAO);
    glBindVertexArray(VAO);

            // Атрибут с координатами
            glBindBuffer(GL_ARRAY_BUFFER, VBO);
            int posLoc = glGetAttribLocation(shaderProgram, "position");
            glVertexAttribPointer(posLoc, 3, GL_FLOAT, GL_FALSE, 0, (GLvoid*)0);
            glEnableVertexAttribArray(posLoc);
            // Атрибут с цветом
            glBindBuffer(GL_ARRAY_BUFFER, colorBO);
            int colorLoc = glGetAttribLocation(shaderProgram, "color");
            glVertexAttribPointer(colorLoc, 3, GL_FLOAT, GL_FALSE, 0, (GLvoid*)0);
            glEnableVertexAttribArray(colorLoc);
            // Тексдура
            //glBindTexture(GL_TEXTURE_2D, texture);
            glBindBuffer(GL_ARRAY_BUFFER, texBO);
            int texLoc = glGetAttribLocation(shaderProgram, "texCoord");
            glVertexAttribPointer(texLoc, 2, GL_FLOAT, GL_FALSE, 0, (GLvoid*)0);
            glEnableVertexAttribArray(texLoc);

            //glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture);
            //int sampler2DLoc = glGetUniformLocation(shaderProgram, "ourTexture");
            //glUniform1i(sampler2DLoc, 0);

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IBO);


    // Отвязываем VAO (НЕ EBO)
    glBindVertexArray(0);

    /* Loop until the user closes the window */
    while (!glfwWindowShouldClose(window)){
        /* Render here */


        //glEnable(GL_DEPTH_TEST); // включает использование буфера глубины
        //glDepthFunc(GL_LEQUAL); // определяет работу буфера глубины: более ближние объекты перекрывают дальние
        //glDepthFunc(GL_LESS);// Фрагмент будет выводиться только в том, случае, если он находится ближе к камере, чем предыдущий



        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


     // Включаем блендинг
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glUseProgram(shaderProgram);

        GLint alphaLoc = glGetUniformLocation(shaderProgram, "alpha");
        glUniform1f(alphaLoc, 1.0f);

        GLuint MVPLoc = glGetUniformLocation(shaderProgram, "MVP");
        glUniformMatrix4fv(MVPLoc, 1, GL_FALSE, glm::value_ptr(MVP));
        //glUniformMatrix4fv(transformLoc, 1, GL_FALSE, &trans[0][0]);


        glBindVertexArray(VAO);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);

        glDisable(GL_BLEND);

        glfwPollEvents();
        // Сбрасываем буферы
        glfwSwapBuffers(window);
    }

    glfwTerminate();
    return 0;
}

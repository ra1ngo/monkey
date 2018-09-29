//вместо #pragma once
#ifndef _MONKEY_H_
#define _MONKEY_H_

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


class Monkey        /*имя класса*/
{
    private:          /* список свойств и методов для использования внутри класса */


    public:           /* список методов доступных другим функциям и объектам программы */
        //////////////////////////////
        /* ВЛОЖЕННЫЕ КЛАССЫ "Monkey"*/
        //////////////////////////////
        class Viewport      /*Viewport*/
        {
            protected:


            public:
                int width, height;

                Viewport(int w,int h){
                    this->width = w;
                    this->height = h;
                    glViewport(0, 0, this->width, this->height);
                }
        };

        class Screen        /*Screen*/
        {
            protected:


            public:
                GLFWwindow* context;
                Viewport* viewport;

                Screen(){
                    // Открыть окно и создать в нем контекст OpenGL
                    this->context = glfwCreateWindow(640, 480, "Hello World", NULL, NULL);
                    //GLFWwindow* window = glfwCreateWindow(800, 600, "LearnOpenGL", null, null);
                    if( this->context == NULL ){
                        fprintf( stderr, "Невозможно открыть окно GLFW. Если у вас Intel GPU, то он не поддерживает версию 3.3." );
                        glfwTerminate();
                        //return -1;
                    }

                    int width, height;
                    glfwGetFramebufferSize(this->context, &width, &height);
                    this->viewport = (new Viewport(width, height));
                }

                Screen(Viewport *v){
                    // Открыть окно и создать в нем контекст OpenGL
                    this->context = glfwCreateWindow(640, 480, "Hello World", NULL, NULL);
                    //GLFWwindow* window = glfwCreateWindow(800, 600, "LearnOpenGL", null, null);
                    if( this->context == NULL ){
                        fprintf( stderr, "Невозможно открыть окно GLFW. Если у вас Intel GPU, то он не поддерживает версию 3.3." );
                        glfwTerminate();
                        //return -1;
                    }

                    this->viewport = v;
                }
        };





        class Texture      /*Texture*/
        {
            protected:

            public:

                class Shader
                {
                    private:
                        GLuint vertexShader;
                        GLuint fragmentShader;

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

                    public:
                        GLuint shaderProgram;
                        std::string vertex;
                        std::string fragment;
                        Shader(){
                            this->vertex =
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

                            this->fragment =
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
                            //prepareProgram
                            const char *source;
                            std::cout << "1" <<  std::endl;
                            //int length;
                            source = this->vertex.c_str();
                            std::cout << "2" << std::endl;
                            //length = shader_vertex_source.size();
                            this->vertexShader = createShader(GL_VERTEX_SHADER, source);
                            std::cout << "3" <<  std::endl;
                            //int length1;
                            source = this->fragment.c_str();
                            //length1 = shader_fragment_source.size();
                            this->fragmentShader = createShader(GL_FRAGMENT_SHADER, source);

                            std::cout << "10" <<  std::endl;

                            //linkProgram
                            this->shaderProgram = glCreateProgram();
                            glAttachShader(this->shaderProgram, this->vertexShader);
                            glAttachShader(this->shaderProgram, this->fragmentShader);
                            glLinkProgram(this->shaderProgram);

                            //отладка
                            GLint status;
                            glGetProgramiv(this->shaderProgram, GL_LINK_STATUS, &status);
                            if(status == GL_FALSE) {
                                GLint length;
                                glGetProgramiv(this->shaderProgram, GL_INFO_LOG_LENGTH, &length);
                                std::vector<char> log(length);
                                glGetProgramInfoLog(this->shaderProgram, length, &length, &log[0]);
                                std::cerr << &log[0];
                                //return false;
                            }

                        }

                        void Use() { glUseProgram(this->shaderProgram); }

                };



                class Mesh
                {
                    private:
                        unsigned char *image;
                        GLuint *shaderProgram;
                        GLuint VAO;
                        int width, height;
                    public:
                        //массив вершин прямоугольника
                        GLfloat vertices[12];
                        GLfloat colors[12];
                        GLuint indices[6];
                        GLfloat texCoords[8];

                         //shaderProgram, image, image.width, image.height
                        Mesh(GLuint *s, unsigned char *i, int w, int h ):
                        vertices{
                            // Позиции
                             0.5f,  0.5f, 0.0f, // Верхний правый угол
                             0.5f, -0.5f, 0.0f, // Нижний правый угол
                            -0.5f, -0.5f, 0.0f, // Нижний левый угол
                            -0.5f,  0.5f, 0.0f  // Верхний левый угол
                            },
                        colors{
                            // Цвета
                            1.0f, 0.0f, 0.0f, // Верхний правый угол
                            0.0f, 1.0f, 0.0f, // Нижний правый угол
                            0.0f, 0.0f, 1.0f, // Нижний левый угол
                            1.0f, 0.0f, 1.0f // Верхний левый угол
                        },
                        indices{  // Помните, что мы начинаем с 0!
                            0, 1, 3,   // Первый треугольник
                            1, 2, 3    // Второй треугольник
                        },
                        texCoords{
                            1.0f, 1.0f,     // Верхний правый угол
                            1.0f, 0.0f,      // Нижний правый угол
                            0.0f, 0.0f,     // Нижний левый угол
                            0.0f, 1.0f     // Верхний левый угол
                        }



                        {
                            std::cout << "MeshStart" <<  std::endl;
                            //std::cout << "Array" << this->vertices << std::endl;

                            this->image = i;
                            this->shaderProgram = s;
                            this->width = w;
                            this->height = h;

                            GLuint texture;
                            glGenTextures(1, &texture);
                            glBindTexture(GL_TEXTURE_2D, texture);
                            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, this->image);

                            // Устанавливаем настройки фильтрации и преобразований (на текущей текстуре)
                            //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
                            //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                            glBindTexture(GL_TEXTURE_2D, 0);
                            stbi_image_free(this->image);



                            GLuint VBO;
                            glGenBuffers(1, &VBO);
                            glBindBuffer(GL_ARRAY_BUFFER, VBO);
                            glBufferData(GL_ARRAY_BUFFER, sizeof(this->vertices), this->vertices, GL_STATIC_DRAW);

                            GLuint colorBO;
                            glGenBuffers(1, &colorBO);
                            glBindBuffer(GL_ARRAY_BUFFER, colorBO);
                            glBufferData(GL_ARRAY_BUFFER, sizeof(this->colors), this->colors, GL_STATIC_DRAW);

                            GLuint texBO;
                            glGenBuffers(1, &texBO);
                            glBindBuffer(GL_ARRAY_BUFFER, texBO);
                            glBufferData(GL_ARRAY_BUFFER, sizeof(this->texCoords), this->texCoords, GL_STATIC_DRAW);

                            GLuint IBO;
                            glGenBuffers(1, &IBO);
                            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IBO);
                            glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(this->indices), this->indices, GL_STATIC_DRAW);



                            //////////////////////////
                            /* ПОДГОТОВКА К UPDATE()*/
                            //////////////////////////
                            //Vertex Array Object!!!
                            glGenVertexArrays(1, &this->VAO);
                            glBindVertexArray(this->VAO);

                            // Атрибут с координатами
                            glBindBuffer(GL_ARRAY_BUFFER, VBO);
                            int posLoc = glGetAttribLocation(*this->shaderProgram, "position");
                            glVertexAttribPointer(posLoc, 3, GL_FLOAT, GL_FALSE, 0, (GLvoid*)0);
                            glEnableVertexAttribArray(posLoc);
                            // Атрибут с цветом
                            glBindBuffer(GL_ARRAY_BUFFER, colorBO);
                            int colorLoc = glGetAttribLocation(*this->shaderProgram, "color");
                            glVertexAttribPointer(colorLoc, 3, GL_FLOAT, GL_FALSE, 0, (GLvoid*)0);
                            glEnableVertexAttribArray(colorLoc);
                            // Тексдура
                            //glBindTexture(GL_TEXTURE_2D, texture);
                            glBindBuffer(GL_ARRAY_BUFFER, texBO);
                            int texLoc = glGetAttribLocation(*this->shaderProgram, "texCoord");
                            glVertexAttribPointer(texLoc, 2, GL_FLOAT, GL_FALSE, 0, (GLvoid*)0);
                            glEnableVertexAttribArray(texLoc);

                            //glActiveTexture(GL_TEXTURE0);
                            glBindTexture(GL_TEXTURE_2D, texture);
                            //int sampler2DLoc = glGetUniformLocation(shaderProgram, "ourTexture");
                            //glUniform1i(sampler2DLoc, 0);
                            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IBO);


                            // Отвязываем VAO (НЕ EBO)
                            glBindVertexArray(0);
                        }

                        void draw(){
                            glBindVertexArray(this->VAO);
                            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
                            glBindVertexArray(0);
                        }

                };


                //public
                unsigned char *image;
                int width, height;
                Shader * shader;
                Mesh * mesh;

                //затем убрать это
                glm::mat4 MVP;

                Texture(const char *path){
                    //stbi_set_flip_vertically_on_load(true);
                    //unsigned char *image = stbi_load("res/img.jpg",&widthIMG,&heightIMG,&channels, STBI_rgb);
                    int channels;
                    this->image = stbi_load(path,&this->width,&this->height,&channels, STBI_rgb_alpha);
                    if(this->image == NULL) {
                        std::cout << "loadTexture failed" << image << std::endl;
                        //return false;
                    }
                    this->shader = new Shader();
                    this->mesh = new Mesh(&this->shader->shaderProgram, this->image, this->width, this->height);




                    //затем убрать это
                    glm::vec3 position = glm::vec3(0.0f, 0.5f, 0.0f);
                    glm::vec3 rotation = glm::vec3(0.0f, 0.0f, 1.0f);
                    glm::vec3 scale    = glm::vec3(0.5f, 0.5f, 1.0f);

                    glm::mat4 trans = glm::mat4(1.0f);

                    float angle = 90.0f;

                    trans = glm::translate(trans, position);
                    trans = glm::rotate(trans, glm::radians(angle), rotation);
                    trans = glm::scale(trans, scale);


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

                    this->MVP = projection * CameraMatrix * trans;



                }

                void draw(){
                    // Включаем блендинг
                    glEnable(GL_BLEND);
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                    this->shader->Use();

                    GLint alphaLoc = glGetUniformLocation(this->shader->shaderProgram, "alpha");
                    glUniform1f(alphaLoc, 1.0f);

                    GLuint MVPLoc = glGetUniformLocation(this->shader->shaderProgram, "MVP");
                    glUniformMatrix4fv(MVPLoc, 1, GL_FALSE, glm::value_ptr(MVP));

                    this->mesh->draw();

                    glDisable(GL_BLEND);
                }



        };



        //поля

        //


        Monkey(){     /* конструктор */


            // Инициализируем GLFW
            if( !glfwInit() ){
                fprintf( stderr, "Ошибка при инициализации GLFWn" );
                //return -1;
            }

            // Инициализируем GLEW
            GLenum err = glewInit();
            if (GLEW_OK != err){
              /* Problem: glewInit failed, something is seriously wrong. */
              fprintf(stderr, "Error: %s\n", glewGetErrorString(err));

            }
            fprintf(stdout, "Status: Using GLEW %s\n", glewGetString(GLEW_VERSION));

            // Устанавливаем параметры
            //glfwWindowHint(GLFW_FSAA_SAMPLES, 4); // 4x Сглаживание
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3); // Мы хотим использовать OpenGL 3.3
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE); // To make MacOS happy; should not be needed
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); // Мы не хотим старый OpenGL
            glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);    //Выключение возможности изменения размера окна
            // Включим режим отслеживания нажатия клавиш, для проверки ниже
            //glfwSetInputMode(window, GLFW_STICKY_KEYS, GL_TRUE);

        }

        void setScreen(Screen *s){
            glfwMakeContextCurrent(s->context);
        }

};
#endif

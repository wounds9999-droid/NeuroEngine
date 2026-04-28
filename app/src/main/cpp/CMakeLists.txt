cmake_minimum_required(VERSION 3.22.1)
project("neuro_engine")

# تحديد المسارات يدوياً لضمان الرؤية
set(NCNN_DIR ${CMAKE_CURRENT_SOURCE_DIR}/ncnn)
set(ncnn_DIR ${NCNN_DIR}/lib/cmake/ncnn)

# إخبار CMake أين يجد ملفات الإعدادات
list(APPEND CMAKE_PREFIX_PATH ${ncnn_DIR})

find_package(ncnn REQUIRED)

add_library(neuro_engine SHARED native-lib.cpp)

target_link_libraries(neuro_engine
    ncnn
    android
    log)

cmake_minimum_required(VERSION 3.22.1)
project("neuro_engine")

# 1. مسار المكتبة (اللي نجحنا فيه المرة الماضية)
set(ncnn_DIR ${CMAKE_CURRENT_SOURCE_DIR}/ncnn/${ANDROID_ABI}/lib/cmake/ncnn)
find_package(ncnn REQUIRED)

# 2. السطر السحري الجديد: مسار النصوص (Headers) عشان يختفي خطأ file not found
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/ncnn/${ANDROID_ABI}/include)

add_library(neuro_engine SHARED native-lib.cpp)
target_link_libraries(neuro_engine ncnn android log jnigraphics z)

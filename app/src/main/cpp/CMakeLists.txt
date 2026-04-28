cmake_minimum_required(VERSION 3.22.1)
project("neuro_engine")

set(ncnn_DIR ${CMAKE_CURRENT_SOURCE_DIR}/ncnn/${ANDROID_ABI}/lib/cmake/ncnn)
find_package(ncnn REQUIRED)

add_library(neuro_engine SHARED native-lib.cpp)

target_link_libraries(neuro_engine ncnn vulkan jnigraphics log android)

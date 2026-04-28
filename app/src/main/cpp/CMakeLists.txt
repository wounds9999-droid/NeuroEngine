cmake_minimum_required(VERSION 3.22.1)
project("neuro_engine")

# توجيه المترجم لمجلد include الخاص بـ ncnn
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/ncnn/include)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/ncnn/include/ncnn)

# تحديد مكان ملفات المكتبة الربط (lib)
set(ncnn_DIR ${CMAKE_CURRENT_SOURCE_DIR}/ncnn/lib/cmake/ncnn)
find_package(ncnn REQUIRED)

add_library(neuro_engine SHARED native-lib.cpp)

target_link_libraries(neuro_engine
    ncnn
    android
    log)

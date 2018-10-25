cmake_minimum_required(VERSION 3.12)
set(CMAKE_CXX_STANDARD 14)

find_package(OpenMP)
if (OPENMP_FOUND)
    set (CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${OpenMP_C_FLAGS}")
    set (CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${OpenMP_CXX_FLAGS}")
    set (CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} ${OpenMP_EXE_LINKER_FLAGS}")
endif()

include_directories(.)

add_executable(pi_integral pi_integral.cpp Timer.cpp)
add_executable(pi_integral_omp pi_integral_omp.cpp Timer.cpp)
add_executable(pi_monte pi_monte.cpp Timer.cpp)
add_executable(pi_monte_omp pi_monte_omp.cpp Timer.cpp)

# install_exe.cmake - a script that installs an executable and configuration
# file(s) for a project
#
# PROJECT_NAME is a CMake environment variable that contains the name of the
#   project.
# EXE_NAME is a CMake environment variable that contains the name of the exe
#   to install.
# EXE_DESTINATION is a CMake environment variable that contains the 
#   exe destination.
# EXE_PARAMS is a CMake environment variable that contains the 
#   exe destination.

# ensure exe name defined
if (NOT EXE_NAME)
    set(EXE_NAME ${PROJECT_NAME})
endif(NOT EXE_NAME)

# ensure exe destination defined
if (NOT EXE_DESTINATION)
    set(EXE_DESTINATION "${PROJECT_NAME}")
endif(NOT EXE_DESTINATION)

# ensure exe destination defined
if (NOT EXE_PARAMS)
    set(EXE_PARAMS "${PROJECT_SOURCE_DIR}/params")
endif(NOT EXE_PARAMS)

# ----- INSTALL EXE ----- #
install(TARGETS ${EXE_NAME} DESTINATION ${EXE_DESTINATION})

# ----- COPY PARAMS ----- #
install(FILES ${EXE_PARAMS} DESTINATION ${EXE_DESTINATION})

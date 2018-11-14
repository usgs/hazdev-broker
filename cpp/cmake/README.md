# cmake
This directory contains various CMake scripts that handle common functions like
code coverage, unit testing, documentation generation, etc.

CMake Scripts:
------
* **base.cmake** - a script that sets up basic complier settings and flags.
* **build_exe.cmake** - a script that builds an executable file for a submodule
(such as glass-app)
* **build_lib.cmake** - a script that builds a library file for a submodule
(such as util)
* **cppcheck.cmake** - a script that runs cpp error checks for a submodule prior to
the submodule build
* **cpplint.cmake** - a script that runs cpp linter (code style) checks on to the
submodule build
* **documentation.cmake** - a script that generates DOxygen documentation for a
submodule
* **install_exe.cmake** - a script that installs an executable and configuration
file(s) for a submodule (such as glass-app)
* **install_lib.cmake** - a script that installs a library and header file (s) for a
submodule (such as util)
* **version.cmake** - a CMake script that defines the overall project version.

Library Include Scripts:
------
* **include_librdkafka.cmake** - a CMake script that handles including the
librdkafka library into a submodule or executable build


CMake configured files:
------
* **Doxyfile.in** - a DOxygen configuration file that documentation.cmake configures
and uses during documentation generation
* **project_version.h.in** - a C++ header file configured and used by CMake to provide
the overall project version to the c++ code.
* **package_config.cmake.in** - a CMake script configured and used by CMake to define
CMake variables for a submodule so that it can be included by other CMake builds

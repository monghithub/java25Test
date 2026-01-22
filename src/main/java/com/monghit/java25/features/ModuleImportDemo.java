package com.monghit.java25.features;

import org.springframework.stereotype.Service;

/**
 * Demo de Module Import Declarations (JEP 476 - Final)
 *
 * Java 25 permite importar todos los paquetes exportados por un módulo
 * de forma sucinta usando: import module <module-name>;
 *
 * Ejemplo:
 * import module java.base;
 *
 * Esto importaría todos los paquetes exportados del módulo java.base:
 * - java.lang.*
 * - java.util.*
 * - java.io.*
 * - java.net.*
 * - etc.
 *
 * NOTA: Esta característica es más útil cuando se trabaja con módulos
 * personalizados o librerías modulares. En este ejemplo, mostramos
 * cómo se usaría conceptualmente.
 */
@Service
public class ModuleImportDemo {

    /**
     * Antes de Java 25 (imports tradicionales):
     *
     * import java.util.List;
     * import java.util.Map;
     * import java.util.Set;
     * import java.util.stream.Stream;
     * import java.util.function.Function;
     * // ... muchos más imports
     */

    /**
     * Con Java 25 (module import):
     *
     * import module java.base;
     *
     * Esto importa automáticamente todos los paquetes exportados
     * por el módulo java.base
     */

    /**
     * Ejemplo de uso con módulos personalizados
     */
    public String demonstrateModuleImport() {
        // Si tuviéramos un módulo personalizado, por ejemplo:
        // module com.example.mylib {
        //     exports com.example.mylib.api;
        //     exports com.example.mylib.util;
        //     exports com.example.mylib.model;
        // }
        //
        // Antes:
        // import com.example.mylib.api.*;
        // import com.example.mylib.util.*;
        // import com.example.mylib.model.*;
        //
        // Con Java 25:
        // import module com.example.mylib;

        return """
                Module Import Declarations simplifica:

                1. Importar todos los paquetes de un módulo
                2. Reducir código boilerplate
                3. Facilitar el uso de librerías modulares
                4. Mantener claridad sobre qué módulos se usan

                Ventajas:
                - Código más limpio
                - Menos imports repetitivos
                - Mejor expresividad
                - Facilita refactoring

                Limitaciones:
                - Solo funciona con módulos
                - No importa paquetes no exportados
                - Requiere que el código fuente use módulos
                """;
    }

    /**
     * Comparación de enfoques
     */
    public void compareApproaches() {
        System.out.println("""
                === Comparación de Imports ===

                1. Import específico:
                   import java.util.List;
                   + Explícito
                   + Preciso
                   - Verboso para muchas clases

                2. Wildcard import:
                   import java.util.*;
                   + Corto
                   - Importa TODO el paquete
                   - Puede causar conflictos

                3. Module import (Java 25):
                   import module java.base;
                   + Importa solo paquetes exportados
                   + Documenta dependencia de módulo
                   + Código más limpio
                   - Solo funciona con módulos
                """);
    }

    /**
     * Ejemplo de módulo personalizado
     * (esto iría en module-info.java)
     */
    public String getModuleExample() {
        return """
                // module-info.java
                module com.monghit.features {
                    exports com.monghit.features.api;
                    exports com.monghit.features.service;
                    exports com.monghit.features.model;

                    requires java.base;
                    requires spring.boot;
                }

                // En otra clase:
                import module com.monghit.features;

                // Ahora tiene acceso a:
                // - com.monghit.features.api.*
                // - com.monghit.features.service.*
                // - com.monghit.features.model.*
                """;
    }
}

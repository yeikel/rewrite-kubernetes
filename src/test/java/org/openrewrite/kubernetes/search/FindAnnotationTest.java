/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.kubernetes.search;

import org.junit.jupiter.api.Test;
import org.openrewrite.kubernetes.KubernetesParserTest;

import static org.openrewrite.yaml.Assertions.yaml;

class FindAnnotationTest extends KubernetesParserTest {

    @Test
    void findIfAnnotationExists() {
        rewriteRun(
          spec -> spec.recipe(new FindAnnotation(
            "mycompany.io/annotation",
            null,
            null
          )),
          yaml(
            """
              apiVersion: v1
              kind: Pod
              metadata:
                name: mypod1
                annotations:
                  mycompany.io/annotation: "hasvalue"
              ---
              apiVersion: v1
              kind: Pod
              metadata:
                name: mypod2
                annotations:
                  mycompany.io/annotation: "novalue"
              """,
            """
              apiVersion: v1
              kind: Pod
              metadata:
                name: mypod1
                annotations:
                  ~~(found:mycompany.io/annotation)~~>mycompany.io/annotation: "hasvalue"
              ---
              apiVersion: v1
              kind: Pod
              metadata:
                name: mypod2
                annotations:
                  ~~(found:mycompany.io/annotation)~~>mycompany.io/annotation: "novalue"
              """
          )
        );
    }

    @Test
    void findByAnnotationValue() {
        rewriteRun(
          spec -> spec.recipe(new FindAnnotation(
            "mycompany.io/annotation",
            "has(.*)",
            null
          )),
          yaml(
            """
              apiVersion: v1
              kind: Pod
              metadata:
                name: mypod1
                annotations:
                  mycompany.io/annotation: "hasvalue"
              ---
              apiVersion: v1
              kind: Pod
              metadata:
                name: mypod2
                annotations:
                  mycompany.io/annotation: "novalue"
              ---
              apiVersion: apps/v1
              kind: Deployment
              spec:
                  template:
                      spec:
                          metadata:
                              annotations:
                                  mycompany.io/annotation: "hasvalue"
                          containers:
                          - name: app
                            image: repo/app:latest
                          - name: sidecar
                            image: repo/sidecar:dev
              """,
            """
              apiVersion: v1
              kind: Pod
              metadata:
                name: mypod1
                annotations:
                  ~~(found:has(.*))~~>mycompany.io/annotation: "hasvalue"
              ---
              apiVersion: v1
              kind: Pod
              metadata:
                name: mypod2
                annotations:
                  mycompany.io/annotation: "novalue"
              ---
              apiVersion: apps/v1
              kind: Deployment
              spec:
                  template:
                      spec:
                          metadata:
                              annotations:
                                  ~~(found:has(.*))~~>mycompany.io/annotation: "hasvalue"
                          containers:
                          - name: app
                            image: repo/app:latest
                          - name: sidecar
                            image: repo/sidecar:dev
              """
          )
        );
    }
}

package org.evoleq.math.cat.gradle.optics

import org.gradle.api.Plugin
import org.gradle.api.Project

class OpticsPlugin : Plugin<Project> {

    private val optics = "optics"
    private lateinit var extension: OpticsExtension

    override fun apply(project: Project) {

        extension = project.extensions.create(optics, OpticsExtension::class.java)

        project.task("generateOptics") {
            group = optics
            doLast {

                // Call the function to show the dialog and collect data
                showGenerateOpticsDialog(project, extension)
            }
        }
        project.task("generateOptionsFromExistingSources") {
            group = optics
            doLast {
                project.showGenerateOptionsFromExistingSourcesDialog(extension)
            }
        }
    }
}

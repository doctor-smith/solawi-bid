package org.evoleq.math.cat.gradle.optics

import org.gradle.api.Project
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*


fun showGenerateOpticsDialog(project: Project, extension: OpticsExtension) {
        // Create the main panel
        val panel = JPanel(GridBagLayout())
        val constraints = GridBagConstraints()

        // Package Name Input
        val packageNameField = JTextField(20)
        packageNameField.text = extension.defaultPackage
        constraints.gridx = 0
        constraints.gridy = 0
        constraints.insets = Insets(10, 10, 10, 10)
        panel.add(JLabel("Enter Package Name:"), constraints)

        constraints.gridx = 1
        panel.add(packageNameField, constraints)

        // Class Name Input
        val classNameField = JTextField(20)
        constraints.gridx = 0
        constraints.gridy = 1
        panel.add(JLabel("Enter Class Name:"), constraints)

        constraints.gridx = 1
        panel.add(classNameField, constraints)

        // Properties Panel
        val propertiesPanel = JPanel()
        propertiesPanel.layout = BoxLayout(propertiesPanel, BoxLayout.Y_AXIS)

        // Add initial property input row
        addPropertyRow(propertiesPanel)

        // Add a scroll pane to allow for resizing
        val scrollPane = JScrollPane(propertiesPanel)
        scrollPane.preferredSize = Dimension(800, 400)

        constraints.gridx = 0
        constraints.gridy = 2
        constraints.gridwidth = 2
        constraints.fill = GridBagConstraints.BOTH
        panel.add(scrollPane, constraints)

        // Button to add more property rows
        val addPropertyButton = JButton("Add Property")
        addPropertyButton.addActionListener {
            addPropertyRow(propertiesPanel)
            propertiesPanel.revalidate()
        }

        constraints.gridx = 0
        constraints.gridy = 3
        constraints.gridwidth = 2
        constraints.fill = GridBagConstraints.HORIZONTAL
        panel.add(addPropertyButton, constraints)

        // Show dialog
        val dialog = JOptionPane.showConfirmDialog(null, panel, "Class and Properties", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)

        // If user clicks OK, gather data
        if (dialog == JOptionPane.OK_OPTION) {
            val packageName = packageNameField.text
            val className = classNameField.text
            val properties = gatherProperties(propertiesPanel)



            ClassDescriptor(
                packageName,
                className,
                properties.map { PropertyDescriptor(
                    it["name"]!!,
                    it["type"]!!,
                    Modifier.from(it["modifier"]!!),
                    with(it["default"]){if(this != ""){this} else {null}}
                ) }
            ).writeToFile(project.projectDir.absolutePath + "/src/${extension.sourceSet}/kotlin")





            // Display the collected information for verification
            println("Package Name: $packageName")
            println("Class Name: $className")
            properties.forEach { println("Property: $it") }
        }
    }

    fun addPropertyRow(propertiesPanel: JPanel) {
        // Create a panel for each property row
        val rowPanel = JPanel(GridBagLayout())
        val constraints = GridBagConstraints()
        constraints.insets = Insets(5, 5, 5, 5)

        val propertyNameField = JTextField(10)
        val propertyTypeField = JTextField(10)
        val propertyDefaultField = JTextField(10)

        // Modifier combo box
        val modifierOptions = arrayOf("ReadWrite", "ReadOnly", "Ignore")
        val modifierComboBox = JComboBox(modifierOptions)

        constraints.gridx = 0
        constraints.gridy = 0
        rowPanel.add(JLabel("Name:"), constraints)

        constraints.gridx = 1
        rowPanel.add(propertyNameField, constraints)

        constraints.gridx = 0
        constraints.gridy = 1
        rowPanel.add(JLabel("Type:"), constraints)

        constraints.gridx = 1
        rowPanel.add(propertyTypeField, constraints)

        constraints.gridx = 0
        constraints.gridy = 2
        rowPanel.add(JLabel("Default Value:"), constraints)

        constraints.gridx = 1
        rowPanel.add(propertyDefaultField, constraints)

        constraints.gridx = 0
        constraints.gridy = 3
        rowPanel.add(JLabel("Modifier:"), constraints)

        constraints.gridx = 1
        rowPanel.add(modifierComboBox, constraints)

        propertiesPanel.add(rowPanel)
    }

    fun gatherProperties(propertiesPanel: JPanel): List<Map<String, String>> {
        val properties = mutableListOf<Map<String, String>>()

        for (component in propertiesPanel.components) {
            if (component is JPanel) {
                val fields = component.components.filterIsInstance<JTextField>()
                val comboBox = component.components.filterIsInstance<JComboBox<*>>().firstOrNull()

                if (fields.size >= 3 && comboBox != null) {
                    val property = mapOf(
                        "name" to fields[0].text,
                        "type" to fields[1].text,
                        "default" to fields[2].text,
                        "modifier" to comboBox.selectedItem.toString()
                    )
                    properties.add(property)
                }
            }
        }

        return properties
    }


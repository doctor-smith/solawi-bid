package org.solyton.solawi.bid.pageobjects.module.user.dropdown

import com.microsoft.playwright.Locator
import org.solyton.solawi.bid.DefaultTestDropdown

class OrganizationDropdown(
    override val locator: Locator,
    override val options: Map<String, () -> Locator>
) : DefaultTestDropdown(
     locator,
    options
)

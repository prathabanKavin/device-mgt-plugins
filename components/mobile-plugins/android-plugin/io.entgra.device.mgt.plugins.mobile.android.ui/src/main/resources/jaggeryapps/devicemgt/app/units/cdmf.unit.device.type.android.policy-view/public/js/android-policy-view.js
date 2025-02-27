/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Method to update the visibility (i.e. disabled or enabled view)
 * of grouped input according to the values
 * that they currently possess.
 * @param domElement HTML grouped-input element with class name "grouped-input"
 */
var updateGroupedInputVisibility = function (domElement) {
    if ($(".parent-input:first", domElement).is(":checked")) {
        if ($(".grouped-child-input:first", domElement).hasClass("disabled")) {
            $(".grouped-child-input:first", domElement).removeClass("disabled");
        }
        $(".child-input", domElement).each(function () {
            $(this).prop('disabled', false);
        });
    } else {
        if (!$(".grouped-child-input:first", domElement).hasClass("disabled")) {
            $(".grouped-child-input:first", domElement).addClass("disabled");
        }
        $(".child-input", domElement).each(function () {
            $(this).prop('disabled', true);
        });
    }
};

/**
 * Populates policy configuration to the ui elements.
 *
 * This method will be invoked from the relevant cdmf unit when the edit page gets loaded.
 *
 * @param profileFeatureList  selected configurations.
 */
var polulateProfileOperations = function (profileFeatureList) {
    var selectedConfigurations = androidOperationModule.populateProfile(profileFeatureList);
    $(".wr-advance-operations li.grouped-input").each(function () {
        updateGroupedInputVisibility(this);
    });
    // enabling previously configured options of last update
    for (var i = 0; i < selectedConfigurations.length; ++i) {
        var selectedOperation = selectedConfigurations[i];
        $(".operation-data").filterByData("operation-code", selectedOperation)
            .find(".panel-title .wr-input-control.switch input[type=checkbox]").each(function () {
            $(this).click();
        });
    }
};

// Start of HTML embedded invoke methods
var showAdvanceOperation = function (operation, button) {
    $(button).addClass('selected');
    $(button).siblings().removeClass('selected');
    var hiddenOperation = ".wr-hidden-operations-content > div";
    $(hiddenOperation + '[data-operation="' + operation + '"]').show();
    $(hiddenOperation + '[data-operation="' + operation + '"]').siblings().hide();
};

/**
 * Method to slide down a provided pane upon provided value set.
 *
 * @param selectElement Select HTML Element to consider
 * @param paneID HTML ID of div element to slide down
 * @param valueSet Applicable Value Set
 */
var slideDownPaneAgainstValueSet = function (selectElement, paneID, valueSet) {
    var selectedValueOnChange = $(selectElement).find("option:selected").val();
    if ($(selectElement).is("input:checkbox")) {
        selectedValueOnChange = $(selectElement).is(":checked").toString();
    }

    var i, slideDownVotes = 0;
    for (i = 0; i < valueSet.length; i++) {
        if (selectedValueOnChange == valueSet[i]) {
            slideDownVotes++;
        }
    }
    var paneSelector = "#" + paneID;
    if (slideDownVotes > 0) {
        if (!$(paneSelector).hasClass("expanded")) {
            $(paneSelector).addClass("expanded");
        }
        $(paneSelector).slideDown();
    } else {
        if ($(paneSelector).hasClass("expanded")) {
            $(paneSelector).removeClass("expanded");
        }
        $(paneSelector).slideUp();
        /* now follows the code to reinitialize all inputs of the slidable pane.
         reinitializing input fields into the defaults.*/
        $(paneSelector + " input").each(
            function () {
                if ($(this).is("input:text")) {
                    $(this).val($(this).data("default"));
                } else if ($(this).is("input:password")) {
                    $(this).val("");
                } else if ($(this).is("input:checkbox")) {
                    $(this).prop("checked", $(this).data("default"));
                    // if this checkbox is the parent input of a grouped-input
                    if ($(this).hasClass("parent-input")) {
                        var groupedInput = $(this).parent().parent().parent();
                        updateGroupedInputVisibility(groupedInput);
                    }
                }
            }
        );
        // reinitializing select fields into the defaults
        $(paneSelector + " select").each(
            function () {
                var defaultOption = $(this).data("default");
                $("option:eq(" + defaultOption + ")", this).prop("selected", "selected");
            }
        );
        // collapsing expanded-panes (upon the selection of html-select-options) if any
        $(paneSelector + " .expanded").each(
            function () {
                if ($(this).hasClass("expanded")) {
                    $(this).removeClass("expanded");
                }
                $(this).slideUp();
            }
        );
        // removing all entries of grid-input elements if exist
        $(paneSelector + " .grouped-array-input").each(
            function () {
                var gridInputs = $(this).find("[data-add-form-clone]");
                if (gridInputs.length > 0) {
                    gridInputs.remove();
                }
                var helpTexts = $(this).find("[data-help-text=add-form]");
                if (helpTexts.length > 0) {
                    helpTexts.show();
                }
            }
        );
    }
};

var slideDownPaneAgainstValueSetForRadioButtons = function (selectElement, paneID, valueSet) {
    var selectedValueOnChange = selectElement.value;
    var slideDownVotes = 0;
    for (var i = 0; i < valueSet.length; i++) {
        if (selectedValueOnChange == valueSet[i]) {
            slideDownVotes++;
        }
    }
    var paneSelector = "#" + paneID;
    if (slideDownVotes > 0) {
        $(paneSelector).removeClass("hidden");
    } else {
        $(paneSelector).addClass("hidden");
    }
};

/**
 * Method to switch panes based on the selected radio button.
 *
 * The method will un hide the element with the id (paneIdPrefix + selectElement.value)
 *
 * @param selectElement selected HTML element
 * @param paneIdPrefix  prefix of the id of the pane to un hide.
 * @param valueSet applicable value set
 */
var switchPaneAgainstValueSetForRadioButtons = function (selectElement, paneIdPrefix, valueSet) {
    var selectedValueOnChange = selectElement.value;
    var paneSelector = "#" + paneIdPrefix;
    var pane;
    for (var i = 0; i < valueSet.length; ++i) {
        if (selectedValueOnChange !== valueSet[i]) {
            pane = paneSelector + valueSet[i].toLowerCase();
            if ($(pane).hasClass("expanded")) {
                $(pane).removeClass("expanded");
            }
            $(pane).slideUp();
        } else {
            pane = paneSelector + selectedValueOnChange.toLowerCase();
            if (!$(pane).hasClass("expanded")) {
                $(pane).addClass("expanded");
            }
            $(pane).slideDown();
        }
    }
};
// End of HTML embedded invoke methods

/**
 * Pass a div Id and a check box to view or hide div content based on checkbox value
 */
var changeDivVisibility = function (divId, checkbox) {
    if (checkbox.checked) {
        document.getElementById(divId).style.display= "block";
    } else {
        document.getElementById(divId).style.display= "none";
        $("#" + divId + " input").each(
            function () {
                if ($(this).is("input:text")) {
                    $(this).val(this.defaultValue);
                } else if ($(this).is("input:checkbox")) {
                    $(this).prop("checked", this.defaultChecked);
                }
            }
        );
        $("#" + divId + " select").each(
            function () {
                $(this).val($(this).data("default"));
            }
        );
        $("#" + divId + " .grouped-array-input").each(
            function () {
                var gridInputs = $(this).find("[data-add-form-clone]");
                if (gridInputs.length > 0) {
                    gridInputs.remove();
                }
                var helpTexts = $(this).find("[data-help-text=add-form]");
                if (helpTexts.length > 0) {
                    helpTexts.show();
                }
            }
        );
        $("#" + divId + " .collapse-config").each(
            function() {
                this.style.display = "none";
            }
        );
    }
};

/**
 * This method will display appropriate fields based on wifi type
 * @param {object} wifi type select object
 */
var changeAndroidWifiPolicy = function (select) {
    slideDownPaneAgainstValueSet(select, 'control-wifi-password', ['wep', 'wpa', '802eap']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-eap', ['802eap']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-phase2', ['802eap']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-identity', ['802eap']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-anoidentity', ['802eap']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-cacert', ['802eap']);
};

/**
 * This method will display appropriate fields based on wifi EAP type
 * @param {object} wifi eap select object
 * @param {object} wifi type select object
 */
var changeAndroidWifiPolicyEAP = function (select, superSelect) {
    slideDownPaneAgainstValueSet(select, 'control-wifi-password', ['peap', 'ttls', 'pwd', 'fast', 'leap']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-phase2', ['peap', 'ttls', 'fast']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-provisioning', ['fast']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-identity', ['peap', 'tls', 'ttls', 'pwd', 'fast', 'leap']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-anoidentity', ['peap', 'ttls']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-cacert', ['peap', 'tls', 'ttls']);
    if (superSelect.value != '802eap') {
        changeAndroidWifiPolicy(superSelect);
    }
};

var applyDataTable = function() {
    $("#enrollment-app-install-table").datatables_extended({
        ordering: false,
        lengthMenu: [5, 10, 25, 50, 100]
    });
};

$(document).ready(function () {
    var advanceOperations = ".wr-advance-operations";
    $(advanceOperations).on("click", ".wr-input-control.switch", function (event) {
        var operation = $(this).parents(".operation-data").data("operation");
        // prevents event bubbling by figuring out what element it's being called from.
        if (event.target.tagName == "INPUT") {
            var isNonAdvanceOperation = $("input[type='checkbox']", this).hasClass("non-advance-operation");
            if (!isNonAdvanceOperation) {
                var featureConfiguredIcon;
                if ($("input[type='checkbox']", this).is(":checked")) {
                    // add configured-state-icon to the feature
                    featureConfiguredIcon = "#" + operation + "-configured";
                    if ($(featureConfiguredIcon).hasClass("hidden")) {
                        $(featureConfiguredIcon).removeClass("hidden");
                    }
                }
            }
        }
    });

    // add form entry click function for grid inputs
    $(advanceOperations).on("click", "[data-click-event=add-form]", function () {
        var addFormContainer = $("[data-add-form-container=" + $(this).attr("href") + "]");
        var clonedForm = $("[data-add-form=" + $(this).attr("href") + "]").clone().find("[data-add-form-element=clone]")
            .attr("data-add-form-clone", $(this).attr("href"));

        // adding class .child-input to capture text-input-array-values
        $("input, select", clonedForm).addClass("child-input");

        $(addFormContainer).append(clonedForm);
        setId(addFormContainer);
        showHideHelpText(addFormContainer);
    });

    // remove form entry click function for grid inputs
    $(advanceOperations).on("click", "[data-click-event=remove-form]", function () {
        var addFormContainer = $("[data-add-form-container=" + $(this).attr("href") + "]");

        $(this).closest("[data-add-form-element=clone]").remove();
        setId(addFormContainer);
        showHideHelpText(addFormContainer);
    });
});

/**
 * Method to set count id to cloned elements.
 * @param {object} addFormContainer
 */
var setId = function (addFormContainer) {
    $(addFormContainer).find("[data-add-form-clone]").each(function (i) {
        $(this).attr("id", $(this).attr("data-add-form-clone").slice(1) + "-" + (i + 1));
        if ($(this).find(".index").length > 0) {
            $(this).find(".index").html(i + 1);
        }
    });
};

/**
 * Method to set count id to cloned elements.
 * @param {object} addFormContainer
 */
var showHideHelpText = function (addFormContainer) {
    var helpText = "[data-help-text=add-form]";
    if ($(addFormContainer).find("[data-add-form-clone]").length > 0) {
        $(addFormContainer).find(helpText).hide();
    } else {
        $(addFormContainer).find(helpText).show();
    }
};
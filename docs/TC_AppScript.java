function validation_AUT_Priority(product) {
  var Num_AUT_Modules_Submodules = {};  
  var mismatchModules = [];

  var sheetName_Master = product + " MASTER REGRESSION";
  var sheetName_AUT = product + " AUT PRIORITY";

   if (!validateRegressionSuiteLinks(sheetName_Master, sheetName_AUT)) {
    return; // Stop execution if validation fails
  }

  var sheet_AUT_Priority = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(sheetName_AUT);  
  if (!sheet_AUT_Priority) {
    SpreadsheetApp.getUi().alert('THE AUT PRIORITY SHEET "' + sheet_AUT_Priority + '" DOES NOT EXIST.');
    return;
  }
  var col_AUT_Module = sheet_AUT_Priority.getRange('A2:A').getValues();
  var col_AUT_Submodule = sheet_AUT_Priority.getRange('B2:B').getValues();

  var values_AUT_Module = col_AUT_Module.flat().filter(function(value) {
    return typeof value === 'string' && value.trim() !== '';  // Filter to only non-empty strings
  });



  var values_AUT_Submodule = col_AUT_Submodule.flat().filter(function(value) {
    return typeof value === 'string' && value.trim() !== '';
  });


  
  var unique_AUT_Module_Name = [...new Set(values_AUT_Module)];
  var count_Unique_AUT_Modules = unique_AUT_Module_Name.length;

  Logger.log('Unique Modules: ' + unique_AUT_Module_Name);
  Logger.log('Number of Unique Modules: ' + count_Unique_AUT_Modules);

  for (var i = 0; i < values_AUT_Module.length; i++) {
    var AUT_ModuleName = values_AUT_Module[i];
    var AUT_SubmoduleName = values_AUT_Submodule[i];

    if (AUT_ModuleName) {
      if (!Num_AUT_Modules_Submodules[AUT_ModuleName]) {
        Num_AUT_Modules_Submodules[AUT_ModuleName] = 0;
      }
      
    
      if (AUT_SubmoduleName) {
        Num_AUT_Modules_Submodules[AUT_ModuleName]++;
      }
    }
  }

    Logger.log('Submodule count from "AUT PRIORITY": ' + JSON.stringify(Num_AUT_Modules_Submodules));

    
  for (var moduleName_In_AUT in Num_AUT_Modules_Submodules) {
    var expected_AUT_Submodule_Count = Num_AUT_Modules_Submodules[moduleName_In_AUT];
    
    
    var actual_Master_Submodule_Count = pullRegressionData(moduleName_In_AUT,sheetName_Master);
     if (actual_Master_Submodule_Count === null) {
      // If the Master sheet is not found, show an alert and stop the execution.
      SpreadsheetApp.getUi().alert('THE MASTER REGRESSION SHEET"' + sheetName_Master + '" DOES NOT EXISTS.');
      return;
    }

    
    if (expected_AUT_Submodule_Count !== actual_Master_Submodule_Count ) {
            mismatchModules.push( moduleName_In_AUT + ' (SUBMODULES IN '+ product +' AUT PRIORITY: ' + expected_AUT_Submodule_Count + ', SUBMODULES IN ' +product+' MASTER REGRESSION: ' + actual_Master_Submodule_Count + ')');
    }
  }


  

    if (mismatchModules.length > 0) {
    var mismatchMessage = "THEHRO .. YE RANKING NAHI HO SAKTI .. !! :\n\n ALL SUBMODULES ARE NOT PRESENT. \n\n RECOMMENDING TO RANK ONLY AFTER LISTING ALL SUBMODULES. :\n\n CHECK FOLLOWING MODULES :\n\n" + mismatchModules.join('\n\n');
    SpreadsheetApp.getUi().alert(mismatchMessage);
  } else {
    // If no mismatches, show success message
    SpreadsheetApp.getUi().alert("BINDAAS .. !! ALL SUBMODULES ARE PRESENT. PEACEFULLY GO AHEAD AND RANK.");
  }
    
    }
  

  function validateRegressionSuiteLinks(sheetName_Master, sheetName_AUT) {
  var sheet_Master = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(sheetName_Master);
  var sheet_AUT = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(sheetName_AUT);

  if (!sheet_Master) {
    SpreadsheetApp.getUi().alert('THE MASTER REGRESSION SHEET "' + sheetName_Master + '" DOES NOT EXIST.');
    return false;
  }
  if (!sheet_AUT) {
    SpreadsheetApp.getUi().alert('THE AUT PRIORITY SHEET "' + sheetName_AUT + '" DOES NOT EXIST.');
    return false;
  }

  // Get Regression Suite Links and Submodule Counts from the Master sheet
  var col_Master_RegressionSuite = sheet_Master.getRange('A2:A').getValues().flat(); // Column A
  var col_Master_SubmoduleCount = sheet_Master.getRange('H2:H').getValues().flat(); // Column H

  // Filter Regression Suite Links where Submodule Count > 0
  var valid_Master_Links = col_Master_RegressionSuite.filter((link, index) => {
    return col_Master_SubmoduleCount[index] > 0 && typeof link === 'string' && link.trim() !== '';
  });

  // Get Regression Suite Links from the AUT Priority sheet
  var col_AUT_RegressionSuite = sheet_AUT.getRange('A2:A').getValues().flat().filter(link => link.trim() !== '');

  // Check for missing links in AUT Priority
  var missingLinks = valid_Master_Links.filter(link => !col_AUT_RegressionSuite.includes(link));

  // Check for extra or duplicate links in AUT Priority
  var extraLinks = col_AUT_RegressionSuite.filter(link => !valid_Master_Links.includes(link));

  // Throw error if there are missing, extra, or duplicate links
  if (missingLinks.length > 0 || extraLinks.length > 0 ) {
    var errorMessage = 'THEHRO .. YE RANKING NAHI HO SAKTI .. !! :\n\n ALL MODULES ARE NOT PRESENT. \n\n RECOMMENDING TO RANK ONLY AFTER LISTING ALL MODULES. :\n\n';
    if (missingLinks.length > 0) {
      errorMessage += 'FOLLOWING MODULES MUST BE PRESENT, AS THERE ARE SUBMODULES IN THEM, WHICH ARE YET TO BE PRIORITISED:\n\n' + missingLinks.join('\n\n') + '\n\n';
    }
    if (extraLinks.length > 0) {
      errorMessage += 'FOLLOWING MODULES ARE NOT NEEDED. EITHER THE SUBMODULES ARE ALL AUTOMATED OR NOT READY TO BE AUTOMATED:\n\n' + extraLinks.join('\n\n') + '\n\n';
    }
    
    SpreadsheetApp.getUi().alert(errorMessage);
    return false; // Stop execution
  }

  // If validation passes
  Logger.log("ALL MODULES ARE PRESENT.");
  return true;
}

  

function pullRegressionData(AUT_ModuleName, sheet_Master_Regression) {
  // Open the active spreadsheet
var sheet_Master = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(sheet_Master_Regression);  
  if (!sheet_Master) {
    // Instead of showing an alert, return -1 or null to indicate that the sheet wasn't found.
    return null;
  }
  // Get the range for Regression Suite Name (Column A) and Submodule Count (Column C)

  var lastRow = sheet_Master.getLastRow(); // Get the last row to avoid fetching unnecessary empty rows
  var col_Master_Module = sheet_Master.getRange(2, 1, lastRow - 1, 1).getValues(); 
  var col_Master_SubModule = sheet_Master.getRange(2, 8, lastRow - 1, 1).getValues();

 var Num_Module_Submodule_Master = 0;
  for (var i = 0; i < col_Master_Module.length; i++) {
    var currentModule = col_Master_Module[i][0];
    var currentSubmoduleCount = col_Master_SubModule[i][0];

    if (currentModule === AUT_ModuleName) {
      Num_Module_Submodule_Master += currentSubmoduleCount;
    }
  }
  Logger.log('Submodule Count for AUT_ModuleName ("' + AUT_ModuleName + '"): ' + Num_Module_Submodule_Master);
  return Num_Module_Submodule_Master;
  
}





function getUserSheetNames() {
  var ui = SpreadsheetApp.getUi();
  
  // Prompt the user to enter the Master Sheet name
  var productPrompt = ui.prompt('PRODUCT NAME', 'PLEASE ENTER THE PRODUCT NAME (TA, SA, NUC, DEL, O2D, ACTIVE TEACH)', ui.ButtonSet.OK_CANCEL);
  if (productPrompt.getSelectedButton() != ui.Button.OK) {
    ui.alert('ACTION CANCELED JI !!');
    return;
  }
  var product = productPrompt.getResponseText().trim();

  // Log the values for debugging
  Logger.log('Product: ' + product);

  // Call the validation function with the provided sheet names
  validation_AUT_Priority(product);
}


function onOpen() {
  var ui = SpreadsheetApp.getUi();
  ui.createMenu('RUN BEFORE PRIORITISING')
  .addItem('ENTER PRODUCT NAME', 'getUserSheetNames')
    .addItem('ALL SUBMODULES ARE LISTED ??', 'getUserSheetNames')
    .addToUi();
}

# **Dynamic Test Execution for Targeted Automation**  

## **Overview**  
This solution addresses two key automation challenges:  
1. Selectively running test suites for specific application modules (like Login or Device Vitals).  
2. Filtering tests by product types (Lead, Propel, Pinnacle) for better test coverage management.  

Both challenges are solved using a **custom listener-based implementation** in TestNG, reducing dependency on extensive group configurations.

---

## **Problem 1: Running Specific Modules for Sanity/Regression**  
### Current:  
- Handled using groups where every method in a module had a group with the module name (`login`, `deviceVitals`).  
- Resulted in an increasing number of groups and complex maintenance.  

### Solution:  
- Implemented a generic TestNG listener using `IMethodInterceptor`.  
- Command-line-based test selection allows dynamic execution without bloating the group configurations.  

---

## **Problem 2: Running for Specific Product Types**  
### Current:  
- Intersection of product type and suite required excessive groups (`LeadSanity`, `PropelSanity`, `LeadRegression`, etc.).  
- Resulted in 10+ groups per method.  

### Solution:  
- Reduced group count by categorizing methods under only 5 groups (2 for suite level, 3 for product types).  
- Used the listener to dynamically filter tests for selected product types.  

---

## **Usage Instructions**  
### Run Sanity Suite for Selected Modules:  
```bash
clean test -Dapplication=ta_app -Dsuite=sanity -DModule=Login,DeviceVitals -DproductType=lead


import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassSelection implements IMethodInterceptor {

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        String testclass = System.getProperty("testclass");
        String testGroups = System.getProperty("testGroups");
        String productType = System.getProperty("productType");

        if (testclass == null || testclass.isEmpty()) {
            List<String> groups = getGroups(testGroups, productType);
            return filterMethodsByGroups(methods, groups);
        }
        else {
            List<String> classNames = getClassNames(testclass);
            List<String> groups = getGroups(testGroups, productType);
            return filterMethods(methods, classNames, groups);
        }
    }

    private List<String> getClassNames(String testclass) {
        if (testclass == null || testclass.isEmpty()) {

            return new ArrayList<>();  // Return empty list if no classes are specified
        }
        return Arrays.asList(testclass.split(","));
    }

    private List<String> getGroups(String testGroups, String productType) {
        List<String> groups = new ArrayList<>();
        if (testGroups != null && !testGroups.isEmpty()) {
            groups.addAll(Arrays.asList(testGroups.toLowerCase().split(",")));
        }
        if (productType != null && !productType.isEmpty()) {
            groups.add(productType.toLowerCase());
        }
        return groups;
    }

    private List<IMethodInstance> filterMethods(List<IMethodInstance> methods, List<String> classNames, List<String> groups) {
        List<IMethodInstance> result = new ArrayList<>();

        for (IMethodInstance method : methods) {
            String fullClassName = method.getMethod().getTestClass().getName();
            String simpleClassName = getSimpleClassName(fullClassName);

            boolean isClassMatched = classNames.isEmpty() || classNames.contains(simpleClassName.toLowerCase());
            boolean areGroupsMatched = areGroupsMatched(method.getMethod(), groups);

            if (isClassMatched && areGroupsMatched) {
                result.add(method);
            }
        }

        return result;
    }


    private List<IMethodInstance> filterMethodsByGroups(List<IMethodInstance> methods, List<String> groups) {
        List<IMethodInstance> result = new ArrayList<>();

        for (IMethodInstance method : methods) {
            boolean areGroupsMatched = areGroupsMatched(method.getMethod(), groups);
            if (areGroupsMatched) {
                result.add(method);
            }
        }

        return result;
    }

    private String getSimpleClassName(String fullClassName) {
        return fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
    }

    private boolean areGroupsMatched(ITestNGMethod testNGMethod, List<String> groups) {
        String[] methodGroups = testNGMethod.getGroups();
        List<String> methodGroupsList = new ArrayList<>(Arrays.asList(methodGroups));
        methodGroupsList.replaceAll(String::toLowerCase);
        return methodGroupsList.containsAll(groups);
    }
}

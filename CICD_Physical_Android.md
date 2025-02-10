# **Continuous Integration for Physical Android Devices**  

## **Overview**  
This project automates the process of transferring `.apk` builds from a remote Jenkins server to a local machine and executing test suites on physical Android devices, eliminating manual interventions and improving testing efficiency.  

---

## **Problem**  
- Manual build installation and test execution were time-consuming and error-prone.  
- Automation code had to reside locally for execution, creating maintenance overhead.  

---

## **Solution**  
- Automated `.apk` transfer from Jenkins to a local machine using secure tunnels (`ngrok`).  
- Triggered test execution directly on physical Android devices.  
- Removed the need for local automation code maintenance.  

---

## **Implementation Details**  
- **CI Tools:** Jenkins (Freestyle & Pipeline Jobs)  
- **Scripting:** Groovy, Jenkins API, Bitbucket API  
- **Plugins:** Parameterized Remote Trigger, Pipeline Syntax  
- **Tunnel Setup:** Ngrok for secure communication between remote and local Jenkins  

---

## **Key Features**  
- On-demand `.apk` retrieval from Dev Jenkins  
- Automatic installation on Android devices  
- Seamless execution of test suites  

---

## **Resources**  
- [View Full Presentation](./docs/CICD_Physical_Android.pptx)  

---

## **Future Improvements**  
- Enhanced logging for better debugging  
- Integration with cloud-based device farms  

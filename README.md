# PaperCrunch
Android application which uses Optical Character Recognition to extract text from images. It then backs this data up to Google Drive. There are two use cases for this application - 

1) DOCUMENT SCANNING - The extracted text is saved in the form of a .doc file in Google Drive. 

2) RECEIPT SCANNING - The receipt is analyzed (there's an extra layer of image processing to detect items and prices), and after the user has edited the results of the analysis, the receipt is digitized and can be viewed in the app. The app uses Google drive to back up receipt data. This feature is still in beta mode.

Since all data is backed up to Google Drive, the app authenticates the user using Google Sign In.

Known Issues - 

-> If the folder which contains PaperCrunch Documents is renamed, the app creates a new folder to store Documents

-> Deletion of items is slightly buggy.

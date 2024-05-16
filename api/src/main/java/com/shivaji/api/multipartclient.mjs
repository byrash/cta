import fetch from 'node-fetch';
import fs from 'fs';

fetch('http://localhost:8080/download')
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.text()
            .then(responseText => ({response, responseText})); // Pass both response and responseText
    })
    .then(({response, responseText}) => { // Receive both response and responseText
        // console.log('Response text:', responseText); // Log the entire response text

        // Find the boundary marker within the response text
        const boundaryMatch = responseText.match(/boundary=(.+)/g);
        if (!boundaryMatch) {
            throw new Error('Boundary marker not found in response text');
        }
        let localText = responseText;

        boundaryMatch.forEach((boundry) => {
            if (!localText) {
                return
            }
            let boundaryEnd = "--" + boundry.replace("boundary=", '').trim()
            let parts = localText.split(boundaryEnd)
            if (parts.length > 0) {
                localText = parts[1];
            } else {
                localText = null;
            }
            let part = parts[0];
            // Split headers and content
            const [headers, content] = part.split('\r\n\r\n');

            // Parse headers
            const contentTypeHeader = headers.match(/Content-Type: (.+);/);
            const contentType = contentTypeHeader ? contentTypeHeader[1].trim() : 'text/plain';

            // Parse content
            const contentData = content.trim();

            // Do something with the content based on the content type
            if (contentType === 'text/plain') {
                console.log('Text content:', contentData);
            } else if (contentType === 'application/json') {
                console.log('JSON content:', JSON.parse(contentData));
            } else if (contentType.startsWith('application/pdf')) {
                // Save PDF content to a file
                const filename = `output_${Date.now()}.pdf`;
                fs.writeFileSync(filename, contentData, 'binary');
                console.log(`PDF content saved to ${filename}`);
                //TODO: Handle Stream of data coming in
            } else {
                // Handle other content types
                console.log('Other content type:', contentType, 'Content: ', content);
            }
        })
    })
    .catch(error => {
        console.error('There was a problem with the fetch operation:', error);
    });




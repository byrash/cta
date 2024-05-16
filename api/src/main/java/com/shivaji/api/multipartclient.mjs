import fetch from 'node-fetch';
import fs from 'fs';

fetch('http://localhost:8080/download')
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.text()
            .then(responseText => ({ response, responseText })); // Pass both response and responseText
    })
    .then(({ response, responseText }) => { // Receive both response and responseText
        // console.log('Response text:', responseText); // Log the entire response text

        // Extract the boundary marker from the Content-Type header
        const contentTypeHeader = response.headers.get('Content-Type');
        const boundaryMatch = contentTypeHeader.match(/boundary=(.+)/);
        if (!boundaryMatch) {
            throw new Error('Boundary marker not found in Content-Type header');
        }
        const boundary = '--' + boundaryMatch[1];
        console.log('Boundary marker:', boundary); // Log the boundary marker

        // Split the response text into parts based on the boundary marker
        const parts = responseText.split(boundary);
        console.log('Parts:', parts); // Log the parts array

        // Remove first and last parts (empty parts)
        parts.shift();
        parts.pop();

        // Parse each part
        for (const part of parts) {
            // Split headers and content
            const [headers, content] = part.split('\r\n\r\n');

            // Parse headers
            const contentTypeHeader = headers.match(/Content-Type: (.+)/);
            const contentType = contentTypeHeader ? contentTypeHeader[1] : 'text/plain';

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
            } else {
                // Handle other content types
                console.log('Other content type:', contentType);
            }
        }
    })
    .catch(error => {
        console.error('There was a problem with the fetch operation:', error);
    });


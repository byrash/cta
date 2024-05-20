import fetch from 'node-fetch';
import fs from 'fs';
import writeFile from 'fs/promises';

fetch('http://localhost:8080/download')
    .then((response) => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.text().then((responseText) => ({ response, responseText })); // Pass both response and responseText
    })
    .then(({ response, responseText }) => {
        // Receive both response and responseText
        // console.log('Response text:', responseText); // Log the entire response text

        // Find the boundary marker within the response text
        const boundaryMatch = responseText.match(/boundary=(.+)/g);
        if (!boundaryMatch) {
            throw new Error('Boundary marker not found in response text');
        }
        let boundary = boundaryMatch[0].replace('boundary=', '').trim();
        let boundaryStart = '--' + boundary;
        let data = responseText.split(boundaryStart);

        data.splice(1).forEach((blockOfData) => {
            // Split headers and content
            const [headers, content] = blockOfData.split('\r\n\r\n');
            if (!content || !headers) {
                return;
            }

            // Parse headers
            const contentTypeHeader = headers.match(/Content-Type: (.+)/);
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
        });
    })
    .catch((error) => {
        console.error('There was a problem with the fetch operation:', error);
    });

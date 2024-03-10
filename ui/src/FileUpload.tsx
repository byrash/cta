import './App.css';
import React, {useState} from 'react';
import axios from 'axios';


function FileUpload(props: any) {
    const [file, setFile] = useState<File>()

    function handleChange(event: any) {
        setFile(event.target.files[0])
    }

    function handleSubmit(event: any) {
        event.preventDefault()
        const url = 'http://localhost:8080/api/upload';
        const formData = new FormData();
        // @ts-ignore
        formData.append('file', file);
        // @ts-ignore
        formData.append('fileName', file.name);
        const config = {
            headers: {
                'content-type': 'multipart/form-data',
                'Access-Control-Allow-Origin': '*'
            },
        };
        axios.post(url, formData, config).then((response) => {
            props.onFileUpload(response.data)
        }).catch(reason => console.log(reason));
    }

    return (
        <div className="App">
            <form onSubmit={handleSubmit}>
                <h1>File Upload</h1>
                <input type="file" onChange={handleChange}/>
                <button type="submit">Upload</button>
            </form>
        </div>
    );
}

export default FileUpload;
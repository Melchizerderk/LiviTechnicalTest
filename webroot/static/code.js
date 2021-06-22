const listContainer = document.querySelector('#service-list');
let servicesRequest = new Request('/service');
fetch(servicesRequest)
.then(function(response) { return response.json(); })
.then(function(serviceList) {
  serviceList.forEach(service => {
    var li = document.createElement("li");
    li.appendChild(document.createTextNode("Name : " + service.name + " URL : " + service.url + ' Status : ' + service.status));
    listContainer.appendChild(li);
  });
});

const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {
    let urlAddress = document.querySelector('#url-address').value;
    let urlName = document.querySelector('#url-name').value;
    fetch('/service', {
    method: 'post',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    },
  body: JSON.stringify({url:urlAddress, name:urlName})
}).then(res=> location.reload());
}

const deleteButton = document.querySelector('#post-delete');
deleteButton.onclick = event => {
    let urlAddress = document.querySelector('#url-address').value;
    fetch('/delete', {
    method: 'post',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    },
    body: JSON.stringify({url:urlAddress})
    }).then(res => location.reload());
}
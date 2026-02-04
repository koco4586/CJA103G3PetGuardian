const fs = require('fs');
const path = 'c:/CJA103_WebApp/CJA103G3PetGuardian/src/main/resources/templates/frontend/dashboard-bookings.html';
let content = fs.readFileSync(path, 'utf8');
const target = '<button class="btn btn-outline btn-sm">評價保姆</button>';
const replacement = '<button class="btn btn-outline btn-sm" th:onclick="|injectEvalBox(this, ${order.bookingOrderId}, ${order.sitterId})|">評價保姆</button>';
if (content.includes(target)) {
    content = content.replace(target, replacement);
    fs.writeFileSync(path, content, 'utf8');
    console.log('Success: Replaced evaluation button.');
} else {
    console.log('Error: Target string not found.');
}

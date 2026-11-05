const BASE=import.meta.env.VITE_API_URL||'http://localhost:8080/api';
async function request(path,options={}) {
 const response=await fetch(BASE+path,{headers:{'Content-Type':'application/json',...(options.headers||{})},...options});
 if(!response.ok){let message=`Request failed (${response.status})`;try{const data=await response.json();message=data.message||message;}catch{}throw new Error(message);}
 if(response.status===204)return null; return response.json();
}
export const getProducts=(q='',category='')=>{const p=new URLSearchParams();if(q)p.set('q',q);if(category)p.set('category',category);return request('/products'+(p.size?'?'+p:''));};
export const createOrder=order=>request('/orders',{method:'POST',body:JSON.stringify(order)});
export const getOrder=details=>request('/orders/lookup',{method:'POST',body:JSON.stringify(details)});

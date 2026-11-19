const API=`${location.protocol}//${location.hostname}:8081/api`,matches=new Map();
const money=new Intl.NumberFormat('es-CO',{style:'currency',currency:'COP',maximumFractionDigits:0});
let token=localStorage.getItem('livebet_token'),user=JSON.parse(localStorage.getItem('livebet_user')||'null'),authMode='login',selectedMatch=null,currentView='matches';
let rouletteState=null,rouletteSelection=null,rouletteChip=5000,rouletteClockOffset=0,settledRouletteRound=null;
let historyItems=[],historyPage=1,historyLoading=false;
const HISTORY_PAGE_SIZE=10;
const MATCH_STALE_GRACE_MS=10000;
const rouletteOrder=[0,32,15,19,4,21,2,25,17,34,6,27,13,36,11,30,8,23,10,5,24,16,33,1,20,14,31,9,22,18,29,7,28,12,35,3,26];
const redNumbers=new Set([1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36]);
const $=s=>document.querySelector(s),$$=s=>[...document.querySelectorAll(s)],esc=s=>String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));

async function api(path,options={}){const headers={'Content-Type':'application/json',...(options.headers||{})};if(token)headers.Authorization=`Bearer ${token}`;const response=await fetch(API+path,{...options,headers});let body={};try{body=await response.json()}catch{}if(!response.ok){if(response.status===401&&token)logout(false);throw new Error(body.detail||body.error||'No fue posible completar la solicitud')}return body}
function toast(message,error=false){const el=$('#toast');el.textContent=message;el.className='toast show'+(error?' error':'');clearTimeout(toast.timer);toast.timer=setTimeout(()=>el.className='toast',3500)}
function saveSession(data){token=data.token;user=data.user;localStorage.setItem('livebet_token',token);localStorage.setItem('livebet_user',JSON.stringify(user));updateSession()}
function logout(show=true){token=null;user=null;localStorage.removeItem('livebet_token');localStorage.removeItem('livebet_user');updateSession();showView('matches');if(show)toast('Sesión cerrada')}
function updateSession(){const logged=!!user;$('#guestSession').classList.toggle('hidden',logged);$('#userSession').classList.toggle('hidden',!logged);$$('.auth-only').forEach(e=>e.classList.toggle('hidden',!logged));$$('.admin-only').forEach(e=>e.classList.toggle('hidden',!logged||user?.role!=='ADMIN'));if(logged){$('#userName').innerHTML=`<b>${esc(user.name.split(' ')[0])}</b>`;$('#userAvatar').textContent=user.name[0].toUpperCase();$('#userBalance').textContent=money.format(user.balance);$('#myBalance').textContent=money.format(user.balance);$('#rechargeBalance').textContent=money.format(user.balance)}else{$('#myBalance').textContent='—';$('#rechargeBalance').textContent='$0'}render()}
async function refreshMe(){if(!token)return;try{user=await api('/auth/me');localStorage.setItem('livebet_user',JSON.stringify(user));updateSession()}catch{}}
function openAuth(mode='login'){authMode=mode;$('#authDialog').showModal();setAuthMode(mode)}
function setAuthMode(mode){authMode=mode;const reg=mode==='register';$('#nameField').classList.toggle('hidden',!reg);$('#authName').required=reg;$('#authTitle').textContent=reg?'Crea tu cuenta':'Iniciar sesión';$('#authSubmit').textContent=reg?'Crear cuenta y recibir saldo':'Entrar a LiveBet';$('#loginTab').classList.toggle('active',!reg);$('#registerTab').classList.toggle('active',reg);$('#authError').textContent='';$('#authPassword').autocomplete=reg?'new-password':'current-password'}
$('#authForm').addEventListener('submit',async e=>{e.preventDefault();$('#authError').textContent='';const payload={email:$('#authEmail').value.trim(),password:$('#authPassword').value};if(authMode==='register')payload.name=$('#authName').value.trim();try{const data=await api('/auth/'+authMode,{method:'POST',body:JSON.stringify(payload)});saveSession(data);$('#authDialog').close();e.target.reset();toast(authMode==='register'?'¡Cuenta creada! Recibiste $250.000 de bienvenida':'Bienvenido de nuevo')}catch(err){$('#authError').textContent=err.message}});

function score(m){return m.homeScore==null||m.awayScore==null?'':`<div class="scoreboard"><span>${m.homeScore}</span><small>MARCADOR FINAL</small><span>${m.awayScore}</span></div>`}
function clock(m){return `<div class="match-clock" data-match-id="${esc(m.matchId)}" data-deadline="${esc(m.endsAt||'')}"><i>⏱</i><span>Calculando…</span></div>`}
function card(m){const closed=m.status!=='OPEN_FOR_BETS',teams=m.teams||[];return `<article class="match ${closed?'closed':''}"><div class="match-top"><span class="match-tag">${closed?'FINALIZADO':'ABIERTO · EN VIVO'}</span><span class="match-id">${esc(m.matchId)}</span></div>${closed?score(m):clock(m)}<div class="teams"><span class="team">${esc(teams[0])}</span><span class="versus-small">VS</span><span class="team">${esc(teams[1])}</span></div>${m.winner?`<div class="winner">🏆 Ganador: ${esc(m.winner)}</div>`:''}<div class="match-meta"><span>${m.bets||0} apuesta${m.bets===1?'':'s'}</span><b>${money.format(m.totalBet||0)}</b></div>${closed?'':`<div class="team-actions"><button data-bet="${esc(m.matchId)}" data-team="${esc(teams[0])}">Voy con ${esc(teams[0])}</button><button data-bet="${esc(m.matchId)}" data-team="${esc(teams[1])}">Voy con ${esc(teams[1])}</button></div>`}</article>`}
function updateTimers(){const now=Date.now();let removed=false;$$('.match-clock').forEach(el=>{const end=Date.parse(el.dataset.deadline),label=el.querySelector('span');if(!Number.isFinite(end)){label.textContent='Cierre programado';return}if(now-end>MATCH_STALE_GRACE_MS){matches.delete(el.dataset.matchId);removed=true;return}const seconds=Math.max(0,Math.ceil((end-now)/1000));el.classList.toggle('urgent',seconds<=10);label.textContent=seconds?`Termina en 00:${String(seconds).padStart(2,'0')}`:'Confirmando resultado…'});if(removed)render()}
function render(){const open=[],closed=[];let total=0;for(const m of matches.values()){total+=Number(m.totalBet||0);(m.status==='OPEN_FOR_BETS'?open:closed).push(m)}$('#openCount').textContent=open.length;$('#liveCount').textContent=open.length;$('#closedCount').textContent=closed.length;$('#totalBets').textContent=money.format(total);$('#openMatches').innerHTML=open.length?open.sort((a,b)=>String(a.endsAt).localeCompare(String(b.endsAt))).map(card).join(''):'<div class="empty">Preparando el próximo partido…</div>';$('#closedMatches').innerHTML=closed.length?closed.sort((a,b)=>String(b.occurredAt).localeCompare(String(a.occurredAt))).slice(0,9).map(card).join(''):'<div class="empty">Todavía no hay resultados finales.</div>';updateTimers()}
async function loadMatches(silent=false){try{const fresh=await (await fetch('/api/matches',{cache:'no-store'})).json();matches.clear();fresh.forEach(m=>matches.set(m.matchId,m));render()}catch{if(!silent)toast('No se pudieron cargar los partidos',true)}}
function connect(){const ws=new WebSocket(`${location.protocol==='https:'?'wss':'ws'}://${location.host}/ws`);ws.onopen=()=>{$('#connection').classList.add('connected');$('#connection span').textContent='En tiempo real'};ws.onmessage=e=>{const m=JSON.parse(e.data);matches.set(m.matchId,m);render()};ws.onclose=()=>{$('#connection').classList.remove('connected');$('#connection span').textContent='Reconectando…';setTimeout(connect,2000)}}

function initRoulette(){
  $('#rouletteNumbers').innerHTML=Array.from({length:36},(_,i)=>{const n=i+1;return `<button class="number ${redNumbers.has(n)?'red':'black'}" data-rtype="NUMBER" data-rvalue="${n}">${n}</button>`}).join('');
  const sector=360/rouletteOrder.length;
  $('#rouletteWheel').style.background=`conic-gradient(from ${-sector/2}deg,${rouletteOrder.map((n,i)=>`${n===0?'#15955b':redNumbers.has(n)?'#df3f51':'#1e2025'} ${i*sector}deg ${(i+1)*sector}deg`).join(',')})`;
  $('#wheelNumbers').innerHTML=rouletteOrder.map((n,i)=>{
    const angle=i*sector,rad=angle*Math.PI/180,radius=39;
    const left=50+radius*Math.sin(rad),top=50-radius*Math.cos(rad);
    return `<span style="left:${left}%;top:${top}%;transform:translate(-50%,-50%) rotate(${angle}deg)"><b>${n}</b></span>`;
  }).join('');
}
function rouletteLabel(type,value){if(type==='NUMBER')return `Número ${value}`;return {RED:'Rojo',BLACK:'Negro',EVEN:'Par',ODD:'Impar',LOW:'1–18',HIGH:'19–36',FIRST:'1.ª docena',SECOND:'2.ª docena',THIRD:'3.ª docena'}[value]||value}
function rouletteMultiplier(type){return type==='NUMBER'?36:type==='DOZEN'?3:2}
function selectRoulette(type,value,button){
  rouletteSelection={type,value,label:rouletteLabel(type,value),multiplier:rouletteMultiplier(type)};
  $$('[data-rtype]').forEach(el=>el.classList.toggle('selected',el===button));
  $('#rouletteSelection').textContent=rouletteSelection.label;
  $('#roulettePotential').textContent=`Premio potencial: ${money.format(rouletteChip*rouletteSelection.multiplier)}`;
  updateRouletteBetButton();
}
function selectRouletteChip(amount,button){rouletteChip=Number(amount);$$('[data-chip]').forEach(el=>el.classList.toggle('active',el===button));if(rouletteSelection)$('#roulettePotential').textContent=`Premio potencial: ${money.format(rouletteChip*rouletteSelection.multiplier)}`;updateRouletteBetButton()}
function updateRouletteBetButton(){const button=$('#placeRouletteBet'),open=rouletteState?.status==='BETTING';button.disabled=!rouletteSelection||!open;button.textContent=rouletteSelection?`Apostar ${money.format(rouletteChip)}`:'Elige una jugada'}
async function loadRoulette(silent=false){
  try{
    const state=await api('/roulette/round');rouletteState=state;rouletteClockOffset=Date.parse(state.serverTime)-Date.now();renderRoulette();
  }catch(err){if(!silent)$('#rouletteError').textContent=err.message}
}
function renderRoulette(){
  if(!rouletteState?.roundId)return;
  $('#rouletteRoundId').textContent=`RONDA ${rouletteState.roundId.toUpperCase()}`;
  const status={BETTING:'APUESTAS ABIERTAS',SPINNING:'GIRANDO',SETTLED:'RESULTADO'}[rouletteState.status]||'PREPARANDO';
  $('#rouletteStatus').textContent=status;$('#rouletteStatus').className=rouletteState.status.toLowerCase();
  $('#rouletteMessage').textContent=rouletteState.status==='BETTING'?'La mesa está abierta. Haz tu jugada.':rouletteState.status==='SPINNING'?'No va más. La ruleta está girando para todos.':`Resultado de la ronda: ${rouletteState.result} ${rouletteState.resultColor==='RED'?'rojo':rouletteState.resultColor==='BLACK'?'negro':'verde'}.`;
  $('#wheelResult').textContent=rouletteState.status==='SETTLED'?rouletteState.result:'BET';
  $('#rouletteRecent').innerHTML=rouletteState.recentResults?.length?rouletteState.recentResults.map(n=>`<i class="${n===0?'green':redNumbers.has(n)?'red':'black'}">${n}</i>`).join(''):'<i>—</i>';
  if(rouletteState.status!=='BETTING')$$('[data-rtype]').forEach(el=>el.classList.remove('selected'));
  updateRouletteBetButton();
  if(rouletteState.status==='SETTLED'&&settledRouletteRound!==rouletteState.roundId){settledRouletteRound=rouletteState.roundId;if(token){setTimeout(refreshMe,450);setTimeout(()=>loadRouletteBets(true),450)}}
}
function updateRouletteAnimation(){
  if(!rouletteState?.roundId)return;
  const now=Date.now()+rouletteClockOffset,closes=Date.parse(rouletteState.closesAt),resolves=Date.parse(rouletteState.resolvesAt),next=Date.parse(rouletteState.nextRoundAt);
  let remaining,label,angle=0;
  if(rouletteState.status==='BETTING'){remaining=Math.max(0,closes-now);label='CIERRA EN'}
  else if(rouletteState.status==='SPINNING'){remaining=Math.max(0,resolves-now);label='RESULTADO EN';const duration=resolves-closes,p=Math.min(1,Math.max(0,(now-closes)/duration)),eased=1-Math.pow(1-p,3),index=Math.max(0,rouletteOrder.indexOf(rouletteState.result));angle=eased*(2160-index*(360/37))}
  else{remaining=Math.max(0,next-now);label='NUEVA RONDA EN';const index=Math.max(0,rouletteOrder.indexOf(rouletteState.result));angle=2160-index*(360/37)}
  $('#rouletteWheel').style.transform=`rotate(${angle}deg)`;
  $('#rouletteTimer').textContent=`${label} ${String(Math.ceil(remaining/1000)).padStart(2,'0')}s`;
}
async function placeRouletteBet(){
  if(!user){openAuth('login');toast('Inicia sesión para jugar en la ruleta');return}
  if(!rouletteSelection||rouletteState?.status!=='BETTING')return;
  const button=$('#placeRouletteBet');button.disabled=true;$('#rouletteError').textContent='';
  try{const data=await api('/roulette/bets',{method:'POST',body:JSON.stringify({roundId:rouletteState.roundId,selectionType:rouletteSelection.type,selectionValue:rouletteSelection.value,amount:rouletteChip})});user.balance=data.balance;localStorage.setItem('livebet_user',JSON.stringify(user));updateSession();toast(`${data.selection}: apuesta de ${money.format(data.amount)} confirmada`);loadRouletteBets(true);loadRoulette(true)}catch(err){$('#rouletteError').textContent=err.message}finally{updateRouletteBetButton()}
}
async function loadRouletteBets(silent=false){
  if(!token){$('#rouletteBetCount').textContent='0';$('#rouletteBetList').innerHTML='<p>Inicia sesión para ver tus jugadas.</p>';return}
  try{const bets=await api('/roulette/bets');$('#rouletteBetCount').textContent=bets.length;$('#rouletteBetList').innerHTML=bets.length?bets.slice(0,6).map(b=>`<article><div><b>${esc(b.selection)}</b><small>${esc(b.roundId)}</small></div><span>${money.format(b.amount)}</span><em class="${b.status.toLowerCase()}">${b.status==='PENDING'?'EN JUEGO':b.status==='WON'?`+${money.format(b.payout)}`:'SIN PREMIO'}</em></article>`).join(''):'<p>Aún no tienes jugadas en la ruleta.</p>'}catch(err){if(!silent)$('#rouletteError').textContent=err.message}
}

function openBet(matchId,team){if(!user){openAuth('login');toast('Inicia sesión para apostar');return}selectedMatch=matches.get(matchId);if(!selectedMatch||selectedMatch.status!=='OPEN_FOR_BETS'){toast('Este partido ya cerró',true);return}$('#betSummary').innerHTML=`<b>${esc(selectedMatch.teams[0])} vs ${esc(selectedMatch.teams[1])}</b><br><span class="muted">Saldo disponible: ${money.format(user.balance)}</span>`;$('#betTeam').innerHTML=selectedMatch.teams.map(t=>`<option ${t===team?'selected':''}>${esc(t)}</option>`).join('');$('#betError').textContent='';$('#betDialog').showModal()}
$('#betForm').addEventListener('submit',async e=>{e.preventDefault();$('#betError').textContent='';try{const data=await api('/bets',{method:'POST',body:JSON.stringify({matchId:selectedMatch.matchId,betOn:$('#betTeam').value,amount:Number($('#betAmount').value)})});user.balance=data.balance;localStorage.setItem('livebet_user',JSON.stringify(user));updateSession();$('#betDialog').close();toast(`Apuesta ${data.betId} registrada correctamente`)}catch(err){$('#betError').textContent=err.message}});
function formatCardNumber(value){return value.replace(/\D/g,'').slice(0,16).replace(/(.{4})/g,'$1 ').trim()}
function selectRechargeAmount(amount){$('#rechargeAmount').value=amount;$$('[data-recharge-amount]').forEach(button=>button.classList.toggle('active',Number(button.dataset.rechargeAmount)===Number(amount)))}
function resetRecharge(){
  $('#rechargeForm').reset();selectRechargeAmount(50000);$('#rechargeSuccess').classList.add('hidden');$('#rechargeForm').classList.remove('hidden');$('#rechargeError').textContent='';
  $('#cardPreviewNumber').textContent='•••• •••• •••• ••••';$('#cardPreviewName').textContent='TU NOMBRE';$('#cardPreviewExpiry').textContent='MM/AA';
}
$('#cardNumber').addEventListener('input',e=>{e.target.value=formatCardNumber(e.target.value);const digits=e.target.value.replace(/\D/g,'');$('#cardPreviewNumber').textContent=digits?digits.padEnd(16,'•').replace(/(.{4})/g,'$1 ').trim():'•••• •••• •••• ••••'});
$('#cardholder').addEventListener('input',e=>$('#cardPreviewName').textContent=e.target.value.trim().toUpperCase()||'TU NOMBRE');
$('#cardExpiry').addEventListener('input',e=>{let digits=e.target.value.replace(/\D/g,'').slice(0,4);if(digits.length>2)digits=digits.slice(0,2)+'/'+digits.slice(2);e.target.value=digits;$('#cardPreviewExpiry').textContent=digits||'MM/AA'});
$('#cardCvv').addEventListener('input',e=>e.target.value=e.target.value.replace(/\D/g,'').slice(0,4));
$('#rechargeAmount').addEventListener('input',e=>$$('[data-recharge-amount]').forEach(button=>button.classList.toggle('active',Number(button.dataset.rechargeAmount)===Number(e.target.value))));
$('#rechargeForm').addEventListener('submit',async e=>{
  e.preventDefault();const button=$('#rechargeSubmit');$('#rechargeError').textContent='';button.disabled=true;button.textContent='Procesando pago…';
  try{
    await new Promise(resolve=>setTimeout(resolve,700));
    const data=await api('/recharges',{method:'POST',body:JSON.stringify({amount:Number($('#rechargeAmount').value),cardholder:$('#cardholder').value.trim(),cardNumber:$('#cardNumber').value,expiry:$('#cardExpiry').value,cvv:$('#cardCvv').value})});
    user.balance=data.balance;localStorage.setItem('livebet_user',JSON.stringify(user));updateSession();
    $('#rechargeSuccessText').innerHTML=`Se abonaron <b>${money.format(data.amount)}</b> a tu saldo.<br>Tarjeta terminada en ${esc(data.cardLast4)} · Ref. ${esc(data.reference)}`;
    $('#rechargeForm').classList.add('hidden');$('#rechargeSuccess').classList.remove('hidden');$('#cardCvv').value='';toast(`Recarga de ${money.format(data.amount)} aprobada`);
  }catch(err){$('#rechargeError').textContent=err.message}finally{button.disabled=false;button.textContent='Pagar y recargar →'}
});
function renderHistory(){
  const totalPages=Math.max(1,Math.ceil(historyItems.length/HISTORY_PAGE_SIZE));historyPage=Math.min(historyPage,totalPages);
  const labels={ACCEPTED_FOR_PROCESSING:'EN PROCESO',PENDING:'EN JUEGO',WON:'GANADA',LOST:'PERDIDA',REJECTED:'REEMBOLSADA'};
  const items=historyItems.slice((historyPage-1)*HISTORY_PAGE_SIZE,historyPage*HISTORY_PAGE_SIZE);
  $('#historyList').innerHTML=items.length?items.map(item=>`<article class="history-item"><div><span class="history-type ${item.type.toLowerCase()}">${item.type==='PARTIDO'?'PARTIDO':'RULETA'}</span><b>${esc(item.selection)}</b><span class="muted">${esc(item.context)} · ${new Date(item.createdAt).toLocaleString('es-CO')}</span></div><div><b>Apuesta ${money.format(item.amount)}</b>${item.status==='WON'?`<span class="prize">Premio +${money.format(item.payout||0)}</span>`:''}</div><span class="status ${String(item.status).toLowerCase()}">${labels[item.status]||esc(item.status)}</span><span class="muted">${esc(item.id)}</span></article>`).join(''):'<div class="empty">Aún no has realizado apuestas.</div>';
  $('#historyPagination').classList.toggle('hidden',historyItems.length<=HISTORY_PAGE_SIZE);$('#historyPageLabel').textContent=`Página ${historyPage} de ${totalPages}`;$('#historyPrev').disabled=historyPage===1;$('#historyNext').disabled=historyPage===totalPages;
}
async function loadHistory(silent=false){
  if(!user||historyLoading)return;historyLoading=true;if(!silent)$('#historyList').innerHTML='<div class="empty">Cargando historial…</div>';
  try{const [sports,roulette]=await Promise.all([api('/bets'),api('/roulette/bets')]);historyItems=[...sports.map(b=>({type:'PARTIDO',id:b.betId,selection:b.betOn,context:`Partido ${b.matchId}`,amount:b.amount,payout:b.payout,status:b.status,createdAt:b.createdAt})),...roulette.map(b=>({type:'RULETA',id:b.betId,selection:b.selection,context:`Ronda ${b.roundId}`,amount:b.amount,payout:b.payout,status:b.status,createdAt:b.createdAt}))].sort((a,b)=>Date.parse(b.createdAt)-Date.parse(a.createdAt));renderHistory()}catch(err){if(!silent)toast(err.message,true)}finally{historyLoading=false}
}
async function loadUsers(){if(user?.role!=='ADMIN')return;$('#usersList').innerHTML='<div class="empty">Cargando usuarios…</div>';try{const users=await api('/admin/users');$('#usersList').innerHTML=users.map(u=>`<article class="user-item"><div><b>${esc(u.name)}</b><span class="muted">${esc(u.email)} · ${esc(u.id)}</span></div><span class="status">${esc(u.role)}</span><b>${money.format(u.balance)}</b><div class="admin-actions"><button data-admin="balance" data-id="${esc(u.id)}">+$50.000</button><button data-admin="role" data-id="${esc(u.id)}" data-role="${esc(u.role)}">${u.role==='ADMIN'?'Hacer USER':'Hacer ADMIN'}</button><button data-admin="active" data-id="${esc(u.id)}" data-active="${u.active}">${u.active?'Desactivar':'Activar'}</button></div></article>`).join('')}catch(err){toast(err.message,true)}}
async function adminAction(button){const id=button.dataset.id,type=button.dataset.admin;try{if(type==='balance')await api(`/admin/users/${id}/balance`,{method:'PATCH',body:JSON.stringify({amount:50000})});if(type==='role')await api(`/admin/users/${id}/role`,{method:'PATCH',body:JSON.stringify({role:button.dataset.role==='ADMIN'?'USER':'ADMIN'})});if(type==='active')await api(`/admin/users/${id}/active`,{method:'PATCH',body:JSON.stringify({active:button.dataset.active!=='true'})});toast('Usuario actualizado');loadUsers();refreshMe()}catch(err){toast(err.message,true)}}
function showView(view){if(view==='recharge'&&!user){openAuth('login');return}currentView=view;['matches','roulette','history','recharge','admin'].forEach(v=>$(`#${v}View`).classList.toggle('hidden',v!==view));$$('[data-view]').forEach(b=>b.classList.toggle('active',b.dataset.view===view));if(view==='roulette'){loadRoulette();loadRouletteBets(true)}if(view==='history')loadHistory();if(view==='recharge'){resetRecharge();refreshMe()}if(view==='admin')loadUsers();const target=$(`#${view}View`);window.scrollTo({top:Math.max(0,target.offsetTop-75),behavior:'smooth'})}

document.addEventListener('click',e=>{const auth=e.target.closest('[data-open-auth]'),bet=e.target.closest('[data-bet]'),view=e.target.closest('[data-view]'),admin=e.target.closest('[data-admin]'),recharge=e.target.closest('[data-recharge-amount]'),roulette=e.target.closest('[data-rtype]'),chip=e.target.closest('[data-chip]');if(auth)openAuth(auth.dataset.openAuth);if(bet)openBet(bet.dataset.bet,bet.dataset.team);if(view)showView(view.dataset.view);if(admin)adminAction(admin);if(recharge)selectRechargeAmount(recharge.dataset.rechargeAmount);if(roulette&&rouletteState?.status==='BETTING')selectRoulette(roulette.dataset.rtype,roulette.dataset.rvalue,roulette);if(chip)selectRouletteChip(chip.dataset.chip,chip);if(e.target.matches('[data-close]'))e.target.closest('dialog').close();if(e.target.matches('[data-amount]'))$('#betAmount').value=e.target.dataset.amount});
$('#loginTab').onclick=()=>setAuthMode('login');$('#registerTab').onclick=()=>setAuthMode('register');$('#logout').onclick=()=>logout();$('#refreshHistory').onclick=()=>{historyPage=1;loadHistory()};$('#historyPrev').onclick=()=>{if(historyPage>1){historyPage--;renderHistory();window.scrollTo({top:$('#historyView').offsetTop-75,behavior:'smooth'})}};$('#historyNext').onclick=()=>{if(historyPage*HISTORY_PAGE_SIZE<historyItems.length){historyPage++;renderHistory();window.scrollTo({top:$('#historyView').offsetTop-75,behavior:'smooth'})}};$('#heroBet').onclick=()=>document.querySelector('.dashboard').scrollIntoView({behavior:'smooth'});$('#placeRouletteBet').onclick=placeRouletteBet;
initRoulette();updateSession();loadMatches();connect();refreshMe();setInterval(updateTimers,1000);setInterval(()=>{if(currentView==='matches')loadMatches(true)},10000);setInterval(updateRouletteAnimation,50);setInterval(()=>{if(currentView==='roulette')loadRoulette(true)},1500);setInterval(()=>{if(currentView==='history'&&user){loadHistory(true);refreshMe()}},5000);

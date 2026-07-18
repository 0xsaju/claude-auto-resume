// Claude Auto-Resume Cockpit — pure UI over the claude-auto-resume CLI and
// its state file (~/.claude/auto-resume/state.json). This extension never
// spawns or parses Claude Code itself (D21): reads come from state.json,
// writes go through the CLI, so there is exactly one logic path.
'use strict';

const vscode = require('vscode');
const cp = require('child_process');
const fs = require('fs');
const path = require('path');
const os = require('os');

const AR_HOME = path.join(os.homedir(), '.claude', 'auto-resume');
const STATE_FILE = path.join(AR_HOME, 'state.json');
const LOG_FILE = path.join(AR_HOME, 'logs', 'plugin.log');
const INSTALL_CMD =
  'curl -fsSL https://raw.githubusercontent.com/0xsaju/claude-auto-resume/main/install.sh | bash';

let statusItem;
let output;
let pollTimer;

// ---------------------------------------------------------------- helpers --

function workspacePath() {
  const folders = vscode.workspace.workspaceFolders;
  return folders && folders.length ? folders[0].uri.fsPath : undefined;
}

function cliPath() {
  const configured = vscode.workspace
    .getConfiguration('claudeAutoResume')
    .get('cliPath');
  if (configured) return configured;
  // GUI-launched VS Code often lacks ~/.local/bin on PATH — check directly.
  const local = path.join(os.homedir(), '.local', 'bin', 'claude-auto-resume');
  if (fs.existsSync(local)) return local;
  return 'claude-auto-resume';
}

function runCli(args) {
  return new Promise((resolve) => {
    cp.execFile(
      cliPath(),
      args,
      { cwd: workspacePath() || os.homedir() },
      (err, stdout, stderr) => {
        resolve({
          notFound: Boolean(err && err.code === 'ENOENT'),
          code: err ? 1 : 0,
          text: `${stdout || ''}${stderr || ''}`.trim(),
        });
      }
    );
  });
}

function readTask() {
  const ws = workspacePath();
  if (!ws) return undefined;
  try {
    const state = JSON.parse(fs.readFileSync(STATE_FILE, 'utf8'));
    return (state.tasks || {})[ws];
  } catch {
    return undefined;
  }
}

// ------------------------------------------------------------- status bar --

function shortTime(iso) {
  const m = /T(\d{2}:\d{2})/.exec(iso || '');
  return m ? m[1] : '';
}

function refreshStatusBar() {
  const task = readTask();
  if (!task) {
    statusItem.text = '$(circle-outline) auto-resume';
    statusItem.tooltip =
      'claude-auto-resume: no tracked task in this workspace. Click for actions.';
    return;
  }
  const t = shortTime(task.resume_at);
  const auto = task.resume_mode === 'auto';
  const map = {
    waiting: ['$(clock)', auto ? `waiting · auto${t ? ` (probe ${t})` : ''}` : `waiting · ${t}`],
    resuming: ['$(sync~spin)', 'resuming…'],
    running: ['$(play)', 'tracked'],
    'limit-hit': ['$(warning)', 'limit hit'],
    done: ['$(check)', 'done'],
    failed: ['$(error)', 'failed'],
    cancelled: ['$(circle-slash)', 'cancelled'],
  };
  const [icon, label] = map[task.status] || ['$(question)', task.status];
  statusItem.text = `${icon} AR: ${label}`;
  statusItem.tooltip =
    `claude-auto-resume — ${task.status} (${task.importance})` +
    `${task.resume_at ? `\nresume at: ${task.resume_at}` : ''}` +
    `\nresumes used: ${task.resume_count ?? 0}/${task.max_resumes ?? 3}` +
    '\nClick for actions.';
}

function startWatching(context) {
  // state.json is replaced atomically (mv), so watch the directory and
  // keep a slow poll as a fallback for platforms where fs.watch is flaky.
  try {
    if (fs.existsSync(AR_HOME)) {
      const watcher = fs.watch(AR_HOME, () => refreshStatusBar());
      context.subscriptions.push({ dispose: () => watcher.close() });
    }
  } catch {
    /* fall back to polling only */
  }
  pollTimer = setInterval(refreshStatusBar, 5000);
  context.subscriptions.push({ dispose: () => clearInterval(pollTimer) });
}

// --------------------------------------------------------------- commands --

async function showStatus() {
  const res = await runCli(['status']);
  if (res.notFound) return offerInstall();
  output.clear();
  output.appendLine(res.text);
  output.show(true);
}

async function scheduleResume() {
  const pick = await vscode.window.showQuickPick(
    [
      { label: 'auto', description: 'detect the reset and resume (recommended)' },
      { label: '30m', description: 'in 30 minutes' },
      { label: '1h', description: 'in 1 hour' },
      { label: '2h30m', description: 'in 2.5 hours' },
      { label: 'now', description: 'immediately' },
      { label: 'custom…', description: '20:00, 45m, ISO-8601 …' },
    ],
    { placeHolder: 'When should this workspace resume?' }
  );
  if (!pick) return;
  let when = pick.label;
  if (when === 'custom…') {
    when = await vscode.window.showInputBox({
      prompt: 'Resume when? (20:00 | 2h30m | ISO-8601 | now | auto)',
    });
    if (!when) return;
  }
  const res = await runCli(['resume-at', when]);
  if (res.notFound) return offerInstall();
  vscode.window.showInformationMessage(
    res.code === 0 ? res.text.split('\n')[0] : `Scheduling failed: ${res.text}`
  );
  refreshStatusBar();
}

async function cancelTask() {
  const res = await runCli(['cancel']);
  if (res.notFound) return offerInstall();
  vscode.window.showInformationMessage(res.text.split('\n')[0]);
  refreshStatusBar();
}

async function openLog() {
  if (!fs.existsSync(LOG_FILE)) {
    vscode.window.showInformationMessage('No claude-auto-resume log yet.');
    return;
  }
  const doc = await vscode.workspace.openTextDocument(LOG_FILE);
  await vscode.window.showTextDocument(doc, { preview: true });
}

function installCli() {
  const term = vscode.window.createTerminal('claude-auto-resume install');
  term.show();
  term.sendText(INSTALL_CMD, true);
}

async function offerInstall() {
  const choice = await vscode.window.showInformationMessage(
    'The claude-auto-resume terminal tool is not installed.',
    'Install in Terminal',
    'Later'
  );
  if (choice === 'Install in Terminal') installCli();
}

async function showMenu() {
  const task = readTask();
  const items = [
    { label: '$(calendar) Schedule resume', act: scheduleResume },
    { label: '$(info) Show status', act: showStatus },
    ...(task && ['waiting', 'resuming', 'running'].includes(task.status)
      ? [{ label: '$(circle-slash) Cancel task', act: cancelTask }]
      : []),
    { label: '$(output) Open log', act: openLog },
    { label: '$(cloud-download) Install/reinstall terminal tool', act: installCli },
  ];
  const pick = await vscode.window.showQuickPick(items, {
    placeHolder: 'claude-auto-resume',
  });
  if (pick) await pick.act();
}

// --------------------------------------------------------------- lifecycle --

async function activate(context) {
  output = vscode.window.createOutputChannel('Claude Auto-Resume');
  statusItem = vscode.window.createStatusBarItem(
    vscode.StatusBarAlignment.Left,
    50
  );
  statusItem.command = 'claudeAutoResume.menu';
  statusItem.show();
  context.subscriptions.push(output, statusItem);

  context.subscriptions.push(
    vscode.commands.registerCommand('claudeAutoResume.menu', showMenu),
    vscode.commands.registerCommand('claudeAutoResume.status', showStatus),
    vscode.commands.registerCommand('claudeAutoResume.scheduleResume', scheduleResume),
    vscode.commands.registerCommand('claudeAutoResume.cancel', cancelTask),
    vscode.commands.registerCommand('claudeAutoResume.openLog', openLog),
    vscode.commands.registerCommand('claudeAutoResume.installCli', installCli)
  );

  refreshStatusBar();
  startWatching(context);

  // Onboarding: offer the one-command install when the CLI is missing.
  const probe = await runCli(['version']);
  if (probe.notFound) await offerInstall();
}

function deactivate() {}

module.exports = { activate, deactivate };

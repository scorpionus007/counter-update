import { Controller, Get, Param, Query, UseGuards } from '@nestjs/common';
import {
  ApiOkResponse,
  ApiOperation,
  ApiParam,
  ApiQuery,
  ApiSecurity,
  ApiTags,
} from '@nestjs/swagger';
import { PrismaService } from '../../prisma/prisma.service';
import { AdminApiKeyGuard } from './admin-api-key.guard';
import {
  AdminDailySummaryDto,
  AdminDeviceDto,
  AdminStatsDto,
  AdminUnlockEventDto,
  AdminUserSummaryDto,
} from './admin.dto';

@ApiTags('Admin')
@ApiSecurity('AdminKey')
@Controller('admin')
@UseGuards(AdminApiKeyGuard)
export class AdminController {
  constructor(private readonly prisma: PrismaService) {}

  @Get('stats')
  @ApiOperation({ summary: 'Overview counts for all tables' })
  @ApiOkResponse({ type: AdminStatsDto })
  async stats(): Promise<AdminStatsDto> {
    const [users, devices, dailySummaries, unlockEvents] = await Promise.all([
      this.prisma.user.count(),
      this.prisma.device.count(),
      this.prisma.dailyUnlockSummary.count(),
      this.prisma.unlockEvent.count(),
    ]);
    return { users, devices, dailySummaries, unlockEvents };
  }

  @Get('users')
  @ApiOperation({ summary: 'List all testers with today + 7-day stats' })
  @ApiOkResponse({ type: [AdminUserSummaryDto] })
  async listUsers() {
    const users = await this.prisma.user.findMany({
      orderBy: { createdAt: 'desc' },
      include: {
        dailySummaries: {
          orderBy: { date: 'desc' },
          take: 7,
        },
        _count: { select: { unlockEvents: true } },
      },
    });

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    return users.map((user) => {
      const todaySummary = user.dailySummaries.find(
        (s) => s.date.getTime() === today.getTime(),
      );
      const last7Total = user.dailySummaries.reduce((sum, s) => sum + s.unlockCount, 0);

      return {
        userId: user.id,
        name: user.name,
        phone: user.phone,
        kycStatus: user.kycStatus,
        createdAt: user.createdAt.toISOString(),
        todayCount: todaySummary?.unlockCount ?? 0,
        last7DaysTotal: last7Total,
        totalEvents: user._count.unlockEvents,
        recentDays: user.dailySummaries.map((s) => ({
          date: s.date.toISOString().slice(0, 10),
          unlockCount: s.unlockCount,
        })),
      };
    });
  }

  @Get('users/:userId/history')
  @ApiOperation({ summary: 'Full daily history for one user' })
  @ApiParam({ name: 'userId', format: 'uuid' })
  async userHistory(@Param('userId') userId: string) {
    const summaries = await this.prisma.dailyUnlockSummary.findMany({
      where: { userId },
      orderBy: { date: 'desc' },
      take: 30,
    });
    const user = await this.prisma.user.findUnique({ where: { id: userId } });
    return {
      user,
      history: summaries.map((s) => ({
        date: s.date.toISOString().slice(0, 10),
        unlockCount: s.unlockCount,
        payableAmountP: s.payableAmountP,
      })),
    };
  }

  @Get('devices')
  @ApiOperation({ summary: 'List all registered devices' })
  @ApiOkResponse({ type: [AdminDeviceDto] })
  async listDevices(): Promise<AdminDeviceDto[]> {
    const devices = await this.prisma.device.findMany({
      orderBy: { lastSeenAt: 'desc' },
      include: { user: { select: { name: true, phone: true } } },
    });
    return devices.map((d) => ({
      id: d.id,
      userId: d.userId,
      androidDeviceId: d.androidDeviceId,
      integrityVerdict: d.integrityVerdict,
      lastSeenAt: d.lastSeenAt.toISOString(),
      userName: d.user.name,
      userPhone: d.user.phone,
    }));
  }

  @Get('daily-summaries')
  @ApiOperation({ summary: 'List all daily unlock summaries' })
  @ApiQuery({ name: 'limit', required: false, example: 100 })
  @ApiOkResponse({ type: [AdminDailySummaryDto] })
  async listDailySummaries(
    @Query('limit') limit?: string,
  ): Promise<AdminDailySummaryDto[]> {
    const take = Math.min(parseInt(limit || '100', 10) || 100, 500);
    const rows = await this.prisma.dailyUnlockSummary.findMany({
      orderBy: { date: 'desc' },
      take,
      include: { user: { select: { name: true, phone: true } } },
    });
    return rows.map((s) => ({
      userId: s.userId,
      userName: s.user.name,
      userPhone: s.user.phone,
      date: s.date.toISOString().slice(0, 10),
      unlockCount: s.unlockCount,
      payableAmountP: s.payableAmountP,
      capped: s.capped,
      computedAt: s.computedAt.toISOString(),
    }));
  }

  @Get('unlock-events')
  @ApiOperation({ summary: 'List recent unlock events' })
  @ApiQuery({ name: 'limit', required: false, example: 100 })
  @ApiOkResponse({ type: [AdminUnlockEventDto] })
  async listUnlockEvents(
    @Query('limit') limit?: string,
  ): Promise<AdminUnlockEventDto[]> {
    const take = Math.min(parseInt(limit || '100', 10) || 100, 500);
    const rows = await this.prisma.unlockEvent.findMany({
      orderBy: { ts: 'desc' },
      take,
      include: { user: { select: { name: true } } },
    });
    return rows.map((e) => ({
      id: e.id,
      userId: e.userId,
      userName: e.user.name,
      ts: e.ts.toISOString(),
      source: e.source,
    }));
  }
}
